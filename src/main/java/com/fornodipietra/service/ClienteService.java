package com.fornodipietra.service;

import com.fornodipietra.dao.ClienteDAO;
import com.fornodipietra.dao.FidelidadeDAO;
import com.fornodipietra.model.Cliente;
import com.fornodipietra.util.Validador;

import java.util.List;

/**
 * Orquestra as regras de negócio relacionadas a clientes.
 *
 * Responsabilidades:
 *  - Validar campos antes de qualquer operação
 *  - Garantir unicidade do email
 *  - Criar automaticamente o registro de fidelidade ao cadastrar
 *  - Proteger integridade referencial na exclusão
 */
public class ClienteService {

    private final ClienteDAO    clienteDAO;
    private final FidelidadeDAO fidelidadeDAO;

    public ClienteService() {
        this.clienteDAO    = new ClienteDAO();
        this.fidelidadeDAO = new FidelidadeDAO();
    }

    // ─── CADASTRAR ────────────────────────────────────────────────────────────

    /**
     * Valida, insere o cliente e cria automaticamente seu registro
     * no programa de fidelidade Carta d'Oro (Bronze, 0 pontos).
     */
    public void cadastrar(String nome, String telefone, String email) {
        validarCamposCliente(nome, telefone, email);

        // Email deve ser único (id=0 = novo registro)
        if (clienteDAO.emailJaExiste(email.trim().toLowerCase(), 0)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado no sistema.");
        }

        Cliente cliente = new Cliente(nome.trim(), telefone.trim(), email.trim().toLowerCase());
        clienteDAO.inserir(cliente);

        // Cria automaticamente o registro de fidelidade para o novo cliente
        fidelidadeDAO.criarParaCliente(cliente.getId());
    }

    // ─── ATUALIZAR ────────────────────────────────────────────────────────────

    /**
     * Valida e atualiza os dados de um cliente existente.
     * Permite manter o mesmo email se for o próprio cliente sendo editado.
     */
    public void atualizar(int id, String nome, String telefone, String email) {
        buscarPorId(id); // garante que existe

        validarCamposCliente(nome, telefone, email);

        // Unicidade do email ignorando o próprio cliente
        if (clienteDAO.emailJaExiste(email.trim().toLowerCase(), id)) {
            throw new IllegalArgumentException("Este e-mail já está em uso por outro cliente.");
        }

        Cliente cliente = new Cliente(nome.trim(), telefone.trim(), email.trim().toLowerCase());
        cliente.setId(id);
        clienteDAO.atualizar(cliente);
    }

    // ─── EXCLUIR ─────────────────────────────────────────────────────────────

    /**
     * Exclui um cliente apenas se não tiver pedidos vinculados.
     * Apêndice II — seção 6 (integridade referencial).
     */
    public void excluir(int id) {
        buscarPorId(id); // garante que existe

        if (clienteDAO.possuiPedidos(id)) {
            throw new IllegalArgumentException(
                "Este cliente não pode ser excluído pois possui pedidos registrados.\n"
                + "O histórico de pedidos deve ser preservado."
            );
        }
        clienteDAO.excluir(id);
    }

    // ─── CONSULTAS ────────────────────────────────────────────────────────────

    public Cliente buscarPorId(int id) {
        Cliente cliente = clienteDAO.buscarPorId(id);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente com id " + id + " não encontrado.");
        }
        return cliente;
    }

    public Cliente buscarPorEmail(String email) {
        return clienteDAO.buscarPorEmail(email.trim().toLowerCase());
    }

    public List<Cliente> listarTodos() {
        return clienteDAO.listarTodos();
    }

    // ─── RELATÓRIOS ───────────────────────────────────────────────────────────

    /** Relatório: clientes com maior número de pedidos. */
    public List<Cliente> relatorioClientesMaisAtivos() {
        return clienteDAO.listarMaisAtivos();
    }

    // ─── HELPERS PRIVADOS ────────────────────────────────────────────────────

    private void validarCamposCliente(String nome, String telefone, String email) {
        String erroNome = Validador.validarNome(nome);
        if (erroNome != null) throw new IllegalArgumentException(erroNome);

        String erroTelefone = Validador.validarTelefone(telefone);
        if (erroTelefone != null) throw new IllegalArgumentException(erroTelefone);

        String erroEmail = Validador.validarEmail(email);
        if (erroEmail != null) throw new IllegalArgumentException(erroEmail);
    }
}
