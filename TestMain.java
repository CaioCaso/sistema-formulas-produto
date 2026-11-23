import java.util.*;

public class TestMain {
    public static void main(String[] args) {
        System.out.println("=== TESTE 1: Caneta ===");
        testeCaneta();
        System.out.println("\n=== TESTE 2: Computador ===");
        testeComputador();
    }

    private static void testeCaneta() {
        List<ItemFormula> itens = Arrays.asList(
            new ItemFormula("Caneta", null, 1, 0.00),
            new ItemFormula("Tinta", "Caneta", 1, 0.00),
            new ItemFormula("Pigmento", "Tinta", 1, 0.30),
            new ItemFormula("Solvente", "Tinta", 1, 0.50),
            new ItemFormula("Corpo", "Caneta", 1, 0.00),
            new ItemFormula("Plastico", "Corpo", 1, 0.70),
            new ItemFormula("Clip", "Corpo", 1, 0.30),
            new ItemFormula("Tampa", "Caneta", 1, 0.00),
            new ItemFormula("Plastico", "Tampa", 1, 0.50),
            new ItemFormula("Vedacao", "Tampa", 1, 0.20)
        );

        FormulaTree tree = FormulaService.buildFormulaTree(itens);
        System.out.println("Árvore da caneta:");
        System.out.println(tree.toIndentedString());

        double total = FormulaService.calcularTotal(tree);
        System.out.printf("Valor total da caneta: R$ %.2f\n", total);
    }

    private static void testeComputador() {
        List<ItemFormula> itens = Arrays.asList(
            new ItemFormula("Computador", null, 1, 0.00),
            new ItemFormula("Gabinete", "Computador", 1, 0.00),
            new ItemFormula("Placa-Mae", "Gabinete", 1, 0.00),
            new ItemFormula("CPU", "Placa-Mae", 1, 1000.00),
            new ItemFormula("Memoria RAM", "Placa-Mae", 2, 200.00),
            new ItemFormula("Chipset", "Placa-Mae", 1, 150.00),
            new ItemFormula("Fonte de Alimentacao", "Gabinete", 1, 300.00),
            new ItemFormula("Armazenamento", "Gabinete", 1, 0.00),
            new ItemFormula("HDD", "Armazenamento", 1, 250.00),
            new ItemFormula("SSD", "Armazenamento", 1, 400.00),
            new ItemFormula("Monitor", "Computador", 1, 0.00),
            new ItemFormula("Tela LCD", "Monitor", 1, 600.00),
            new ItemFormula("Carcaca", "Monitor", 1, 100.00),
            new ItemFormula("Perifericos", "Computador", 1, 0.00),
            new ItemFormula("Teclado", "Perifericos", 1, 100.00),
            new ItemFormula("Mouse", "Perifericos", 1, 80.00)
        );

        FormulaTree tree = FormulaService.buildFormulaTree(itens);
        System.out.println("Árvore do computador:");
        System.out.println(tree.toIndentedString());

        double total = FormulaService.calcularTotal(tree);
        System.out.printf("Valor total do computador: R$ %.2f\n", total);
    }
}
