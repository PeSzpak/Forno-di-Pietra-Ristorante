package com.fornodipietra.service;

import com.fornodipietra.dao.PratoDAO;
import com.fornodipietra.model.CategoriaPrato;
import com.fornodipietra.model.Prato;
import com.fornodipietra.util.Validador;

import java.math.BigDecimal;
import java.util.List;

/**
 * Orquestra as regras de negócio relacionadas a pratos.
 *
 * Responsabilidades:
 *  - Validar campos antes de qualquer operação no banco
 *  - Garantir unicidade do código do prato
 *  - Proteger integridade referencial na exclusão
 *  - Expor os dados de que a UI precisa
 *
 * Lança IllegalArgumentException com mensagem pronta para o JOptionPane.
 */
public class PratoService {

    private final PratoDAO pratoDAO;

    public PratoService() {
        this.pratoDAO = new PratoDAO();
    }

    // ─── CADASTRAR ────────────────────────────────────────────────────────────

    /**
     * Valida e insere um novo prato.
     * @throws IllegalArgumentException com mensagem de erro se inválido
     */
    public void cadastrar(String codigo, String nome, String precoTexto,
                          String ingredientes, CategoriaPrato categoria) {

        validarCamposPrato(codigo, nome, precoTexto, ingredientes, categoria);

        // Código deve ser único no sistema (id=0 = novo registro)
        if (pratoDAO.codigoJaExiste(codigo.trim().toUpperCase(), 0)) {
            throw new IllegalArgumentException("Código '" + codigo.trim() + "' já está cadastrado.");
        }

        Prato prato = new Prato(
            codigo.trim().toUpperCase(),
            nome.trim(),
            converterPreco(precoTexto),
            ingredientes.trim(),
            categoria,
            true // disponível por padrão ao cadastrar
        );
        pratoDAO.inserir(prato);
    }

    // ─── ATUALIZAR ────────────────────────────────────────────────────────────

    /**
     * Valida e atualiza um prato existente.
     * Permite manter o mesmo código se for o próprio prato sendo editado.
     */
    public void atualizar(int id, String codigo, String nome, String precoTexto,
                          String ingredientes, CategoriaPrato categoria, boolean disponivel) {

        Prato existente = buscarPorId(id);

        validarCamposPrato(codigo, nome, precoTexto, ingredientes, categoria);

        // Verifica unicidade do código ignorando o próprio prato
        if (pratoDAO.codigoJaExiste(codigo.trim().toUpperCase(), id)) {
            throw new IllegalArgumentException("Código '" + codigo.trim() + "' já está em uso por outro prato.");
        }

        existente.setCodigo(codigo.trim().toUpperCase());
        existente.setNome(nome.trim());
        existente.setPreco(converterPreco(precoTexto));
        existente.setIngredientes(ingredientes.trim());
        existente.setCategoria(categoria);
        existente.setDisponivel(disponivel);

        pratoDAO.atualizar(existente);
    }

    // ─── DISPONIBILIDADE ─────────────────────────────────────────────────────

    /**
     * Alterna a disponibilidade de um prato (disponível ↔ indisponível).
     */
    public void alterarDisponibilidade(int id, boolean disponivel) {
        buscarPorId(id); // garante que o prato existe
        pratoDAO.atualizarDisponibilidade(id, disponivel);
    }

    // ─── EXCLUIR ─────────────────────────────────────────────────────────────

    /**
     * Exclui um prato apenas se ele não estiver vinculado a pedidos.
     * Apêndice II — seção 6 (integridade referencial).
     */
    public void excluir(int id) {
        buscarPorId(id); // garante que existe

        if (pratoDAO.possuiPedidos(id)) {
            throw new IllegalArgumentException(
                "Este prato não pode ser excluído pois está vinculado a pedidos.\n"
                + "Dica: marque-o como indisponível em vez de excluir."
            );
        }
        pratoDAO.excluir(id);
    }

    // ─── CONSULTAS ────────────────────────────────────────────────────────────

    public Prato buscarPorId(int id) {
        Prato prato = pratoDAO.buscarPorId(id);
        if (prato == null) {
            throw new IllegalArgumentException("Prato com id " + id + " não encontrado.");
        }
        return prato;
    }

    public List<Prato> listarTodos() {
        return pratoDAO.listarTodos();
    }

    public List<Prato> listarDisponiveis() {
        return pratoDAO.listarDisponiveis();
    }

    public List<Prato> listarPorCategoria(CategoriaPrato categoria) {
        return pratoDAO.listarPorCategoria(categoria);
    }

    public List<Prato> listarIndisponiveis() {
        return pratoDAO.listarIndisponiveis();
    }

    // ─── RELATÓRIOS ───────────────────────────────────────────────────────────

    /** Relatório: pratos mais vendidos (pedidos FINALIZADOS). */
    public List<Object[]> relatorioPratosMaisVendidos() {
        return pratoDAO.listarMaisVendidos();
    }

    // ─── HELPERS PRIVADOS ────────────────────────────────────────────────────

    private void validarCamposPrato(String codigo, String nome, String precoTexto,
                                     String ingredientes, CategoriaPrato categoria) {
        String erroCodigo = Validador.validarCodigo(codigo);
        if (erroCodigo != null) throw new IllegalArgumentException(erroCodigo);

        String erroNome = Validador.validarNome(nome);
        if (erroNome != null) throw new IllegalArgumentException(erroNome);

        String erroPreco = Validador.validarPrecoTexto(precoTexto);
        if (erroPreco != null) throw new IllegalArgumentException(erroPreco);

        String erroIngredientes = Validador.validarDescricaoObrigatoria(ingredientes);
        if (erroIngredientes != null) throw new IllegalArgumentException("Ingredientes: " + erroIngredientes);

        if (categoria == null) {
            throw new IllegalArgumentException("Selecione uma categoria para o prato.");
        }
    }

    private BigDecimal converterPreco(String precoTexto) {
        return new BigDecimal(precoTexto.trim().replace(",", "."));
    }
}
