package com.fornodipietra.util;

import com.fornodipietra.model.Fidelidade;
import com.fornodipietra.model.TierFidelidade;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilitário com a lógica de negócio do programa de fidelidade Carta d'Oro.
 *
 * Centraliza os cálculos de pontos, resgates e validações de elegibilidade
 * para que o FidelidadeService possa usar sem duplicar regras.
 *
 * Todos os métodos são estáticos — não precisa instanciar.
 */
public class TierUtil {

    // Custo em pontos para cada tipo de resgate
    public static final int PONTOS_PARA_DESCONTO     = 500;
    public static final int PONTOS_PARA_PRATO_GRATIS = 500;

    // Valor máximo do prato grátis (regra de negócio do restaurante)
    public static final BigDecimal VALOR_MAXIMO_PRATO_GRATIS = new BigDecimal("35.00");

    // Construtor privado — classe utilitária
    private TierUtil() {}

    // ─────────────────────────────────────────────────────────────────────────
    // 1. CÁLCULO DE PONTOS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcula os pontos a acumular com base no valor do pedido.
     * Regra: R$1,00 = 1 ponto (trunca centavos).
     *
     * Exemplo: R$52,90 → 52 pontos
     */
    public static int calcularPontosGanhos(BigDecimal valorPedido) {
        if (valorPedido == null || valorPedido.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return valorPedido.setScale(0, RoundingMode.FLOOR).intValue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. ELEGIBILIDADE PARA RESGATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verifica se o cliente pode resgatar um desconto percentual.
     * Elegível: tier PRATA ou OURO com pelo menos 500 pontos.
     *
     * @return null se elegível, mensagem de erro se não
     */
    public static String validarElegibilidadeDesconto(Fidelidade fidelidade) {
        if (fidelidade == null) {
            return "Cadastro de fidelidade não encontrado para este cliente.";
        }
        if (!fidelidade.getTier().podeResgatar()) {
            return "Resgate disponível apenas para clientes Prata ou Ouro.\n"
                 + "Acumule mais " + fidelidade.pontosParaProximoTier() + " pontos para subir de tier.";
        }
        if (fidelidade.getPontosAcumulados() < PONTOS_PARA_DESCONTO) {
            return "Pontos insuficientes. Necessário: " + PONTOS_PARA_DESCONTO
                 + " | Disponível: " + fidelidade.getPontosAcumulados();
        }
        return null;
    }

    /**
     * Verifica se o cliente pode resgatar um prato grátis.
     * Elegível: apenas tier OURO com pelo menos 500 pontos.
     *
     * @return null se elegível, mensagem de erro se não
     */
    public static String validarElegibilidadePratoGratis(Fidelidade fidelidade) {
        if (fidelidade == null) {
            return "Cadastro de fidelidade não encontrado para este cliente.";
        }
        if (!fidelidade.getTier().podeResgatarPratoGratis()) {
            return "Resgate de prato grátis disponível apenas para clientes Ouro.\n"
                 + "Tier atual: " + fidelidade.getTier().getDescricao();
        }
        if (fidelidade.getPontosAcumulados() < PONTOS_PARA_PRATO_GRATIS) {
            return "Pontos insuficientes. Necessário: " + PONTOS_PARA_PRATO_GRATIS
                 + " | Disponível: " + fidelidade.getPontosAcumulados();
        }
        return null;
    }

    /**
     * Verifica se um prato é elegível para resgate grátis pelo valor.
     * Regra: prato deve custar até R$35,00.
     *
     * @return null se elegível, mensagem de erro se não
     */
    public static String validarPratoParaResgate(BigDecimal precoPrato) {
        if (precoPrato == null) {
            return "Preço do prato inválido.";
        }
        if (precoPrato.compareTo(VALOR_MAXIMO_PRATO_GRATIS) > 0) {
            return "Este prato custa R$ " + precoPrato
                 + " e ultrapassa o limite de R$ " + VALOR_MAXIMO_PRATO_GRATIS
                 + " para resgate grátis.";
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. CÁLCULO DE DESCONTO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcula o percentual de desconto que o cliente tem direito pelo tier.
     *
     * @return percentual (ex: 5.0 para Prata, 10.0 para Ouro, 0.0 para Bronze)
     */
    public static double obterPercentualDesconto(TierFidelidade tier) {
        return tier.getPercentualDesconto();
    }

    /**
     * Aplica o desconto do tier sobre um valor total de pedido.
     *
     * Exemplo: valor=100.00, tier=PRATA → desconto=5.00 → valorFinal=95.00
     *
     * @return BigDecimal com o valor após desconto, arredondado para 2 casas
     */
    public static BigDecimal aplicarDesconto(BigDecimal valorOriginal, TierFidelidade tier) {
        if (valorOriginal == null || tier.getPercentualDesconto() == 0.0) {
            return valorOriginal;
        }
        BigDecimal percentual = BigDecimal.valueOf(tier.getPercentualDesconto()).divide(BigDecimal.valueOf(100));
        BigDecimal desconto   = valorOriginal.multiply(percentual).setScale(2, RoundingMode.HALF_UP);
        return valorOriginal.subtract(desconto).setScale(2, RoundingMode.HALF_UP);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. FORMATAÇÃO PARA EXIBIÇÃO NA UI
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retorna texto resumido do status de fidelidade para exibição na tela.
     *
     * Exemplo: "🥈 Prata — 740 pts | Próximo tier: 760 pts para Ouro"
     */
    public static String resumoFidelidade(Fidelidade fidelidade) {
        if (fidelidade == null) return "Sem cadastro de fidelidade.";

        TierFidelidade tier   = fidelidade.getTier();
        int pontos            = fidelidade.getPontosAcumulados();
        int pontosParaSubir   = fidelidade.pontosParaProximoTier();

        StringBuilder sb = new StringBuilder();
        sb.append(tier.getDescricao())
          .append(" — ")
          .append(pontos)
          .append(" pontos");

        if (tier == TierFidelidade.OURO) {
            sb.append(" | Nível máximo atingido!");
        } else {
            sb.append(" | Faltam ")
              .append(pontosParaSubir)
              .append(" pts para ")
              .append(TierFidelidade.values()[tier.ordinal() + 1].getDescricao());
        }
        return sb.toString();
    }

    /**
     * Retorna texto dos benefícios disponíveis para resgate.
     * Usado na tela de fidelidade para informar o cliente.
     */
    public static String beneficiosDisponiveis(Fidelidade fidelidade) {
        if (fidelidade == null) return "";

        TierFidelidade tier = fidelidade.getTier();

        if (tier == TierFidelidade.BRONZE) {
            return "Bronze: apenas acúmulo de pontos. Alcance 500 pts para Prata!";
        }
        if (tier == TierFidelidade.PRATA) {
            return "Prata: resgate 5% de desconto no próximo pedido (custa " + PONTOS_PARA_DESCONTO + " pontos).";
        }
        // Ouro
        return "Ouro: resgate 10% de desconto OU um prato grátis até R$35,00 (custa " + PONTOS_PARA_PRATO_GRATIS + " pontos).";
    }
}
