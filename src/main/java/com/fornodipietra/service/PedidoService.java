package com.fornodipietra.service;

import com.fornodipietra.dao.ClienteDAO;
import com.fornodipietra.dao.FidelidadeDAO;
import com.fornodipietra.dao.PedidoDAO;
import com.fornodipietra.dao.PratoDAO;
import com.fornodipietra.model.Fidelidade;
import com.fornodipietra.model.ItemPedido;
import com.fornodipietra.model.Pedido;
import com.fornodipietra.model.Prato;
import com.fornodipietra.model.StatusPedido;
import com.fornodipietra.util.TierUtil;
import com.fornodipietra.util.Validador;

import java.math.BigDecimal;
import java.util.List;

/**
 * Orquestra as regras de negócio relacionadas a pedidos.
 *
 * Responsabilidades:
 *  - Validar disponibilidade dos pratos antes de adicionar ao pedido
 *  - Controlar o fluxo de status: ABERTO → FINALIZADO ou CANCELADO
 *  - Acumular pontos de fidelidade automaticamente ao finalizar
 *  - Incrementar o total de pedidos do cliente ao finalizar
 *  - Bloquear operações inválidas por status (Apêndice II — seção 4.2)
 */
public class PedidoService {

    private final PedidoDAO    pedidoDAO;
    private final PratoDAO     pratoDAO;
    private final ClienteDAO   clienteDAO;
    private final FidelidadeDAO fidelidadeDAO;

    public PedidoService() {
        this.pedidoDAO     = new PedidoDAO();
        this.pratoDAO      = new PratoDAO();
        this.clienteDAO    = new ClienteDAO();
        this.fidelidadeDAO = new FidelidadeDAO();
    }

    // ─── REGISTRAR PEDIDO ────────────────────────────────────────────────────

    /**
     * Cria e persiste um novo pedido com todos os seus itens.
     *
     * Validações aplicadas:
     *  - Cliente deve existir
     *  - Lista de itens não pode estar vazia
     *  - Cada prato deve estar disponível (Apêndice II — seção 4.3)
     *  - Quantidade de cada item deve ser válida
     *
     * @param clienteId  id do cliente que faz o pedido
     * @param itens      lista de itens já montada pela UI
     * @param observacao observação opcional
     * @return o Pedido inserido com id gerado pelo banco
     */
    public Pedido registrar(int clienteId, List<ItemPedido> itens, String observacao) {

        // 1. Cliente existe?
        if (clienteDAO.buscarPorId(clienteId) == null) {
            throw new IllegalArgumentException("Cliente não encontrado. Selecione um cliente válido.");
        }

        // 2. Pedido tem pelo menos um item?
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve ter pelo menos um prato.");
        }

        // 3. Valida cada item
        for (ItemPedido item : itens) {
            Prato prato = pratoDAO.buscarPorId(item.getPratoId());

            if (prato == null) {
                throw new IllegalArgumentException("Prato com id " + item.getPratoId() + " não encontrado.");
            }

            // Prato indisponível não pode entrar no pedido (Apêndice II — 4.3)
            if (!prato.isDisponivel()) {
                throw new IllegalArgumentException(
                    "O prato '" + prato.getNome() + "' está indisponível no momento."
                );
            }

            String erroQtd = Validador.validarQuantidade(item.getQuantidade());
            if (erroQtd != null) {
                throw new IllegalArgumentException("Item '" + prato.getNome() + "': " + erroQtd);
            }

            // Usa o preço atual do prato (grava snapshot no item)
            item.setPratoId(prato.getId());
            item.setPrecoUnitario(prato.getPreco());
        }

        // 4. Valida observação opcional
        String erroObs = Validador.validarObservacaoOpcional(observacao);
        if (erroObs != null) throw new IllegalArgumentException(erroObs);

        // 5. Monta o pedido
        Pedido pedido = new Pedido(clienteId, observacao);
        for (ItemPedido item : itens) {
            pedido.adicionarItem(item);
        }

        // 6. Persiste (transação no DAO — pedido + itens de uma vez)
        pedidoDAO.inserir(pedido);
        return pedido;
    }

    // ─── FINALIZAR PEDIDO ────────────────────────────────────────────────────

    /**
     * Finaliza um pedido ABERTO.
     * Após finalizar:
     *  - Status muda para FINALIZADO
     *  - Pontos de fidelidade são acumulados (R$1 = 1 ponto)
     *  - Total de pedidos do cliente é incrementado
     *
     * @throws IllegalArgumentException se o pedido não puder ser finalizado
     */
    public void finalizar(int pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);

        // Valida transição de status (Apêndice II — seção 4.2)
        String erro = Validador.validarFinalizarPedido(pedido.getStatus());
        if (erro != null) throw new IllegalArgumentException(erro);

        // Pedido precisa ter itens para ser finalizado
        List<ItemPedido> itens = pedidoDAO.buscarItensPorPedido(pedidoId);
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Não é possível finalizar um pedido sem itens.");
        }

        // Atualiza status no banco
        pedidoDAO.atualizarStatus(pedidoId, StatusPedido.FINALIZADO);

        // Acumula pontos de fidelidade (R$1 = 1 ponto)
        int pontosGanhos = TierUtil.calcularPontosGanhos(pedido.getValorTotal());
        if (pontosGanhos > 0) {
            acumularPontos(pedido.getClienteId(), pontosGanhos);
        }

        // Incrementa o contador interno de pedidos do cliente
        clienteDAO.incrementarTotalPedidos(pedido.getClienteId());
    }

    // ─── CANCELAR PEDIDO ─────────────────────────────────────────────────────

    /**
     * Cancela um pedido ABERTO.
     * Pedidos FINALIZADOS não podem ser cancelados (Apêndice II — seção 4.2 e 5).
     *
     * @throws IllegalArgumentException se o pedido não puder ser cancelado
     */
    public void cancelar(int pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);

        // Valida transição de status (Apêndice II — seção 4.2)
        String erro = Validador.validarCancelarPedido(pedido.getStatus());
        if (erro != null) throw new IllegalArgumentException(erro);

        pedidoDAO.atualizarStatus(pedidoId, StatusPedido.CANCELADO);
    }

    // ─── CONSULTAS ────────────────────────────────────────────────────────────

    public Pedido buscarPorId(int id) {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido com id " + id + " não encontrado.");
        }
        return pedido;
    }

    public List<Pedido> listarTodos() {
        return pedidoDAO.listarTodos();
    }

    public List<Pedido> listarPorCliente(int clienteId) {
        return pedidoDAO.listarPorCliente(clienteId);
    }

    public List<ItemPedido> buscarItensDoPedido(int pedidoId) {
        return pedidoDAO.buscarItensPorPedido(pedidoId);
    }

    // ─── RELATÓRIOS ───────────────────────────────────────────────────────────

    /**
     * Relatório: faturamento total em um período.
     * Valida que data inicial não é maior que a final (Apêndice II — seção 7).
     *
     * @param dataInicio formato dd/MM/yyyy
     * @param dataFim    formato dd/MM/yyyy
     */
    public List<Pedido> relatorioFaturamentoPorPeriodo(String dataInicio, String dataFim) {
        String erroIntervalo = Validador.validarIntervaloData(dataInicio, dataFim);
        if (erroIntervalo != null) throw new IllegalArgumentException(erroIntervalo);

        // Converte para formato MySQL (yyyy-MM-dd)
        String inicioMySQL = converterParaFormatoMySQL(dataInicio);
        String fimMySQL    = converterParaFormatoMySQL(dataFim);

        return pedidoDAO.listarPorPeriodo(inicioMySQL, fimMySQL);
    }

    /** Calcula o valor total faturado de uma lista de pedidos. */
    public BigDecimal calcularFaturamentoTotal(List<Pedido> pedidos) {
        BigDecimal total = BigDecimal.ZERO;
        for (Pedido p : pedidos) {
            total = total.add(p.getValorTotal());
        }
        return total;
    }

    // ─── HELPERS PRIVADOS ────────────────────────────────────────────────────

    /** Acumula pontos no registro de fidelidade do cliente e recalcula o tier. */
    private void acumularPontos(int clienteId, int pontos) {
        Fidelidade fidelidade = fidelidadeDAO.buscarPorCliente(clienteId);
        if (fidelidade == null) {
            // Segurança: cria o registro se não existir (não deveria acontecer)
            fidelidadeDAO.criarParaCliente(clienteId);
            fidelidade = fidelidadeDAO.buscarPorCliente(clienteId);
        }
        fidelidade.adicionarPontos(pontos);
        fidelidadeDAO.atualizarPontosETier(
            clienteId,
            fidelidade.getPontosAcumulados(),
            fidelidade.getTier()
        );
    }

    /**
     * Converte data de dd/MM/yyyy para yyyy-MM-dd (formato aceito pelo MySQL).
     * Exemplo: "15/06/2025" → "2025-06-15"
     */
    private String converterParaFormatoMySQL(String data) {
        String[] partes = data.trim().split("/");
        // partes[0]=dia, partes[1]=mes, partes[2]=ano
        return partes[2] + "-" + partes[1] + "-" + partes[0];
    }
}
