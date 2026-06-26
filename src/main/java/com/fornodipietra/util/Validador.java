package com.fornodipietra.util;

import com.fornodipietra.model.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Centraliza todas as regras de validação do Apêndice II do projeto.
 *
 * Todos os métodos são estáticos — não é necessário instanciar esta classe.
 * Cada método retorna uma String com a mensagem de erro, ou null se válido.
 * Isso permite exibir o erro diretamente no JOptionPane sem lógica extra na UI.
 *
 * Exemplo de uso:
 *   String erro = Validador.validarNome(campo.getText());
 *   if (erro != null) {
 *       JOptionPane.showMessageDialog(this, erro);
 *       return;
 *   }
 */
public class Validador {

    private static final int NOME_MIN_CHARS        = 3;
    private static final int NOME_MAX_CHARS        = 100;
    private static final int CODIGO_MAX_CHARS      = 10;
    private static final int DESCRICAO_MAX_CHARS   = 500;
    private static final int TELEFONE_MIN_DIGITOS  = 10;
    private static final int TELEFONE_MAX_DIGITOS  = 11;

    // Construtor privado — classe utilitária, não deve ser instanciada
    private Validador() {}

    // ─────────────────────────────────────────────────────────────────────────
    // 1. NOME (pessoa, empresa, prato, plano)
    // Apêndice II — seção 2.1
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida nome de pessoa, prato ou qualquer campo de nome.
     * Regras: não vazio, mínimo 3 chars, não pode ser só números.
     */
    public static String validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return "O nome não pode ser vazio.";
        }
        String nomeTrimado = nome.trim();
        if (nomeTrimado.length() < NOME_MIN_CHARS) {
            return "O nome deve ter pelo menos " + NOME_MIN_CHARS + " caracteres.";
        }
        if (nomeTrimado.length() > NOME_MAX_CHARS) {
            return "O nome deve ter no máximo " + NOME_MAX_CHARS + " caracteres.";
        }
        // Não pode conter apenas números
        if (nomeTrimado.matches("\\d+")) {
            return "O nome não pode conter apenas números.";
        }
        return null; // válido
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. CÓDIGO DE PRATO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida código do prato (ex: PIZ001, ENT002).
     * Regras: não vazio, máximo 10 chars, sem espaços.
     */
    public static String validarCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return "O código não pode ser vazio.";
        }
        String codigoTrimado = codigo.trim();
        if (codigoTrimado.length() > CODIGO_MAX_CHARS) {
            return "O código deve ter no máximo " + CODIGO_MAX_CHARS + " caracteres.";
        }
        if (codigoTrimado.contains(" ")) {
            return "O código não pode conter espaços.";
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. EMAIL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida formato básico de e-mail.
     * Regras: não vazio, contém @ e domínio válido.
     */
    public static String validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "O e-mail não pode ser vazio.";
        }
        String emailTrimado = email.trim();
        // Verifica formato: algo@algo.algo
        if (!emailTrimado.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            return "E-mail inválido. Use o formato: exemplo@dominio.com";
        }
        if (emailTrimado.length() > 150) {
            return "O e-mail deve ter no máximo 150 caracteres.";
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. TELEFONE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida telefone brasileiro.
     * Aceita formatos: (41) 99999-9999 ou 41999999999 (apenas dígitos).
     */
    public static String validarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return "O telefone não pode ser vazio.";
        }
        // Remove tudo que não é dígito para contar
        String soDigitos = telefone.replaceAll("[^0-9]", "");
        if (soDigitos.length() < TELEFONE_MIN_DIGITOS || soDigitos.length() > TELEFONE_MAX_DIGITOS) {
            return "Telefone inválido. Informe DDD + número (10 ou 11 dígitos).";
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. PREÇO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida preço de um prato.
     * Regras: não nulo, maior que zero, não negativo.
     */
    public static String validarPreco(BigDecimal preco) {
        if (preco == null) {
            return "O preço não pode ser vazio.";
        }
        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            return "O preço deve ser maior que zero.";
        }
        if (preco.compareTo(new BigDecimal("9999.99")) > 0) {
            return "O preço não pode ser maior que R$ 9.999,99.";
        }
        return null;
    }

    /**
     * Valida preço informado como String (vindo de um JTextField).
     * Converte para BigDecimal antes de validar.
     */
    public static String validarPrecoTexto(String precoTexto) {
        if (precoTexto == null || precoTexto.trim().isEmpty()) {
            return "O preço não pode ser vazio.";
        }
        // Aceita tanto ponto quanto vírgula como separador decimal
        String normalizado = precoTexto.trim().replace(",", ".");
        try {
            BigDecimal preco = new BigDecimal(normalizado);
            return validarPreco(preco);
        } catch (NumberFormatException e) {
            return "Preço inválido. Use o formato: 49.90 ou 49,90";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. DESCRIÇÃO / OBSERVAÇÃO / INGREDIENTES
    // Apêndice II — seção 2.2
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida campo obrigatório de descrição (ex: ingredientes).
     */
    public static String validarDescricaoObrigatoria(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return "Este campo não pode ser vazio.";
        }
        if (descricao.trim().length() > DESCRICAO_MAX_CHARS) {
            return "Este campo deve ter no máximo " + DESCRICAO_MAX_CHARS + " caracteres.";
        }
        // Não pode conter apenas símbolos sem letras ou números
        if (!descricao.trim().matches(".*[a-zA-ZÀ-ÿ0-9].*")) {
            return "Este campo contém apenas caracteres inválidos.";
        }
        return null;
    }

    /**
     * Valida campo opcional de observação.
     * Se vazio, retorna null (válido). Se preenchido, aplica as regras.
     */
    public static String validarObservacaoOpcional(String observacao) {
        if (observacao == null || observacao.trim().isEmpty()) {
            return null; // campo opcional — vazio é válido
        }
        if (observacao.trim().length() > DESCRICAO_MAX_CHARS) {
            return "A observação deve ter no máximo " + DESCRICAO_MAX_CHARS + " caracteres.";
        }
        if (!observacao.trim().matches(".*[a-zA-ZÀ-ÿ0-9].*")) {
            return "A observação contém apenas caracteres inválidos.";
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. DATAS
    // Apêndice II — seção 3
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida uma data no formato dd/MM/yyyy.
     * Retorna null se válida, mensagem de erro se inválida.
     */
    public static String validarData(String dataTexto) {
        if (dataTexto == null || dataTexto.trim().isEmpty()) {
            return "A data não pode ser vazia.";
        }
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate.parse(dataTexto.trim(), fmt);
            return null;
        } catch (DateTimeParseException e) {
            return "Data inválida. Use o formato: dd/MM/aaaa";
        }
    }

    /**
     * Valida que a data inicial não é posterior à data final.
     * Apêndice II — seção 7 (relatórios).
     * Ambas devem estar no formato dd/MM/yyyy.
     */
    public static String validarIntervaloData(String dataInicioTexto, String dataFimTexto) {
        String erroInicio = validarData(dataInicioTexto);
        if (erroInicio != null) return "Data inicial: " + erroInicio;

        String erroFim = validarData(dataFimTexto);
        if (erroFim != null) return "Data final: " + erroFim;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate inicio = LocalDate.parse(dataInicioTexto.trim(), fmt);
        LocalDate fim    = LocalDate.parse(dataFimTexto.trim(), fmt);

        if (inicio.isAfter(fim)) {
            return "A data final não pode ser anterior à data inicial.";
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. QUANTIDADE (itens do pedido)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida quantidade de um item de pedido.
     * Regras: maior que zero, não negativo, razoável (max 99).
     */
    public static String validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            return "A quantidade deve ser maior que zero.";
        }
        if (quantidade > 99) {
            return "A quantidade não pode ser maior que 99.";
        }
        return null;
    }

    /**
     * Valida quantidade informada como String (vindo de JTextField).
     */
    public static String validarQuantidadeTexto(String quantidadeTexto) {
        if (quantidadeTexto == null || quantidadeTexto.trim().isEmpty()) {
            return "A quantidade não pode ser vazia.";
        }
        try {
            int quantidade = Integer.parseInt(quantidadeTexto.trim());
            return validarQuantidade(quantidade);
        } catch (NumberFormatException e) {
            return "Quantidade inválida. Informe apenas números inteiros.";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. STATUS — conflito de transição
    // Apêndice II — seção 4.2
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida se um pedido pode ser finalizado a partir do status atual.
     */
    public static String validarFinalizarPedido(StatusPedido statusAtual) {
        if (statusAtual == StatusPedido.FINALIZADO) {
            return "Operação inválida: este pedido já está finalizado.";
        }
        if (statusAtual == StatusPedido.CANCELADO) {
            return "Operação inválida: não é possível finalizar um pedido cancelado.";
        }
        return null;
    }

    /**
     * Valida se um pedido pode ser cancelado a partir do status atual.
     */
    public static String validarCancelarPedido(StatusPedido statusAtual) {
        if (statusAtual == StatusPedido.FINALIZADO) {
            return "Operação inválida: não é possível cancelar um pedido já finalizado.";
        }
        if (statusAtual == StatusPedido.CANCELADO) {
            return "Operação inválida: este pedido já está cancelado.";
        }
        return null;
    }
}
