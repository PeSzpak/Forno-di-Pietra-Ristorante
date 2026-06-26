package com.fornodipietra.service;

import com.fornodipietra.dao.FidelidadeDAO;
import com.fornodipietra.dao.PratoDAO;
import com.fornodipietra.model.Fidelidade;
import com.fornodipietra.model.Prato;
import com.fornodipietra.model.ResgatesFidelidade;
import com.fornodipietra.model.TierFidelidade;
import com.fornodipietra.util.TierUtil;
import com.fornodipietra.util.Validador;

import java.math.BigDecimal;
import java.util.List;

/**
 * Orquestra as regras de negócio do programa de fidelidade Carta d'Oro.
 *
 * Responsabilidades:
 *  - Consultar o saldo e tier de um cliente
 *  - Validar elegibilidade para resgate
 *  - Processar resgate de desconto (Prata/Ouro)
 *  - Processar resgate de prato grátis (exclusivo Ouro, prato ≤ R$35)
 *  - Fornecer dados para os relatórios de fidelidade
 */
public class FidelidadeService {

    private final FidelidadeDAO fidelidadeDAO;
    private final PratoDAO      pratoDAO;

    public FidelidadeService() {
        this.fidelidadeDAO = new FidelidadeDAO();
        this.pratoDAO      = new PratoDAO();
    }

    // ─── CONSULTAS ────────────────────────────────────────────────────────────

    /**
     * Retorna o registro de fidelidade de um cliente.
     * @throws IllegalArgumentException se o cliente não tiver cadastro de fidelidade
     */
    public Fidelidade buscarPorCliente(int clienteId) {
        Fidelidade fidelidade = fidelidadeDAO.buscarPorCliente(clienteId);
        if (fidelidade == null) {
            throw new IllegalArgumentException(
                "Este cliente não possui cadastro no programa Carta d'Oro.\n"
                + "Verifique se o cliente foi cadastrado corretamente."
            );
        }
        return fidelidade;
    }

    /**
     * Retorna texto formatado com tier, pontos e benefícios do cliente.
     * Pronto para exibir diretamente em um JLabel ou JTextArea.
     */
    public String resumoDoCliente(int clienteId) {
        Fidelidade fidelidade = buscarPorCliente(clienteId);
        return TierUtil.resumoFidelidade(fidelidade)
             + "\n"
             + TierUtil.beneficiosDisponiveis(fidelidade);
    }

    /**
     * Retorna o histórico de resgates de um cliente.
     */
    public List<ResgatesFidelidade> listarResgatesDoCliente(int clienteId) {
        return fidelidadeDAO.listarResgatesPorCliente(clienteId);
    }

    // ─── RESGATE DE DESCONTO ─────────────────────────────────────────────────

    /**
     * Processa resgate de desconto percentual.
     *
     * Regras:
     *  - Tier deve ser PRATA ou OURO
     *  - Cliente deve ter pelo menos 500 pontos
     *  - Desconta 500 pontos e recalcula o tier
     *  - Registra o resgate no histórico
     *
     * @return percentual de desconto a aplicar (5.0 para Prata, 10.0 para Ouro)
     * @throws IllegalArgumentException se o cliente não for elegível
     */
    public double resgatarDesconto(int clienteId) {
        Fidelidade fidelidade = buscarPorCliente(clienteId);

        // Valida elegibilidade (TierUtil — regras centralizadas)
        String erro = TierUtil.validarElegibilidadeDesconto(fidelidade);
        if (erro != null) throw new IllegalArgumentException(erro);

        double percentual = TierUtil.obterPercentualDesconto(fidelidade.getTier());
        BigDecimal percentualBD = BigDecimal.valueOf(percentual);

        // Desconta os pontos e recalcula o tier
        fidelidade.descontarPontos(TierUtil.PONTOS_PARA_DESCONTO);
        fidelidadeDAO.atualizarPontosETier(
            clienteId,
            fidelidade.getPontosAcumulados(),
            fidelidade.getTier()
        );

        // Registra o resgate no histórico
        ResgatesFidelidade resgate = new ResgatesFidelidade(
            clienteId,
            percentualBD,
            TierUtil.PONTOS_PARA_DESCONTO
        );
        fidelidadeDAO.registrarResgate(resgate);

        return percentual;
    }

    /**
     * Calcula o valor final de um pedido após aplicar o desconto do tier.
     * Usado pela UI para exibir o valor com desconto antes de confirmar.
     *
     * @param valorOriginal valor total do pedido sem desconto
     * @param tier          tier do cliente
     * @return valor após desconto
     */
    public BigDecimal calcularValorComDesconto(BigDecimal valorOriginal, TierFidelidade tier) {
        return TierUtil.aplicarDesconto(valorOriginal, tier);
    }

    // ─── RESGATE DE PRATO GRÁTIS ─────────────────────────────────────────────

    /**
     * Processa resgate de prato grátis (exclusivo tier Ouro).
     *
     * Regras:
     *  - Tier deve ser OURO
     *  - Cliente deve ter pelo menos 500 pontos
     *  - Prato deve custar no máximo R$35,00
     *  - Prato deve estar disponível
     *  - Desconta 500 pontos e recalcula o tier
     *  - Registra o resgate no histórico com o prato escolhido
     *
     * @param pratoId id do prato escolhido para o resgate
     * @return o Prato resgatado (para exibir na confirmação)
     * @throws IllegalArgumentException se alguma regra for violada
     */
    public Prato resgatarPratoGratis(int clienteId, int pratoId) {
        Fidelidade fidelidade = buscarPorCliente(clienteId);

        // Valida elegibilidade do cliente
        String erroCliente = TierUtil.validarElegibilidadePratoGratis(fidelidade);
        if (erroCliente != null) throw new IllegalArgumentException(erroCliente);

        // Valida o prato
        Prato prato = pratoDAO.buscarPorId(pratoId);
        if (prato == null) {
            throw new IllegalArgumentException("Prato não encontrado.");
        }
        if (!prato.isDisponivel()) {
            throw new IllegalArgumentException(
                "O prato '" + prato.getNome() + "' está indisponível no momento."
            );
        }

        // Valida o valor do prato (máximo R$35,00)
        String erroPrato = TierUtil.validarPratoParaResgate(prato.getPreco());
        if (erroPrato != null) throw new IllegalArgumentException(erroPrato);

        // Desconta os pontos e recalcula o tier
        fidelidade.descontarPontos(TierUtil.PONTOS_PARA_PRATO_GRATIS);
        fidelidadeDAO.atualizarPontosETier(
            clienteId,
            fidelidade.getPontosAcumulados(),
            fidelidade.getTier()
        );

        // Registra o resgate no histórico
        ResgatesFidelidade resgate = new ResgatesFidelidade(
            clienteId,
            pratoId,
            TierUtil.PONTOS_PARA_PRATO_GRATIS
        );
        fidelidadeDAO.registrarResgate(resgate);

        return prato;
    }

    // ─── RELATÓRIOS ───────────────────────────────────────────────────────────

    /**
     * Relatório: distribuição de clientes por tier (Bronze/Prata/Ouro).
     * Retorna List<Object[]> onde cada linha é: [tier(String), total(int)]
     */
    public List<Object[]> relatorioClientesPorTier() {
        return fidelidadeDAO.contarPorTier();
    }

    /**
     * Relatório: histórico de resgates em um período.
     * Valida o intervalo antes de consultar.
     *
     * @param dataInicio formato dd/MM/yyyy
     * @param dataFim    formato dd/MM/yyyy
     */
    public List<Object[]> relatorioResgatesPorPeriodo(String dataInicio, String dataFim) {
        String erroIntervalo = Validador.validarIntervaloData(dataInicio, dataFim);
        if (erroIntervalo != null) throw new IllegalArgumentException(erroIntervalo);

        String inicioMySQL = converterParaFormatoMySQL(dataInicio);
        String fimMySQL    = converterParaFormatoMySQL(dataFim);

        return fidelidadeDAO.listarResgatesPorPeriodo(inicioMySQL, fimMySQL);
    }

    // ─── HELPER PRIVADO ──────────────────────────────────────────────────────

    private String converterParaFormatoMySQL(String data) {
        String[] partes = data.trim().split("/");
        return partes[2] + "-" + partes[1] + "-" + partes[0];
    }
}
