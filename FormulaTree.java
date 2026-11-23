import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Estrutura hierárquica da fórmula de um produto.
 * Cada componente pode ter qualquer quantidade de componentes filhos.
 */
public class FormulaTree {

    public static class Node {
        ItemFormula item;
        List<Node> children = new ArrayList<>();

        public Node(ItemFormula item) {
            this.item = item;
        }
    }

    private Node root;

    public FormulaTree() {
        root = null;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(ItemFormula product) {
        root = new Node(product);
    }

    public Node find(String name) {
        return findRec(root, name);
    }

    private Node findRec(Node node, String name) {
        if (node == null) {
            return null;
        }
        if (node.item.getNome().equalsIgnoreCase(name)) {
            return node;
        }
        for (Node child : node.children) {
            Node found = findRec(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public boolean insertChild(String parentName, ItemFormula child) {
        Node parent = find(parentName);
        if (parent == null) {
            return false;
        }
        parent.children.add(new Node(child));
        return true;
    }

    public String toIndentedString() {
        StringBuilder result = new StringBuilder();
        buildString(root, 0, result);
        return result.toString();
    }

    private void buildString(Node node, int level, StringBuilder result) {
        if (node == null) {
            return;
        }
        result.append("  ".repeat(level))
                .append("- ")
                .append(node.item)
                .append("\n");
        for (Node child : node.children) {
            buildString(child, level + 1, result);
        }
    }

    public double calcularTotal() {
        return calcularTotal(root, 1);
    }

    private double calcularTotal(Node node, int quantidadeDoPai) {
        if (node == null) {
            return 0.0;
        }
        int quantidadeDoItem = node.item.getQuantidade();
        double total = quantidadeDoItem * quantidadeDoPai * node.item.getValorUnitario();
        int quantidadeDosFilhos = quantidadeDoItem * quantidadeDoPai;
        for (Node child : node.children) {
            total += calcularTotal(child, quantidadeDosFilhos);
        }
        return total;
    }

    public Map<String, Integer> calcularQuantidades(int quantidadeProdutos) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        acumularQuantidades(root, quantidadeProdutos, quantities);
        return quantities;
    }

    private void acumularQuantidades(
            Node node,
            int quantidadeDoPai,
            Map<String, Integer> quantities
    ) {
        if (node == null) {
            return;
        }
        int quantidadeDoItem = node.item.getQuantidade() * quantidadeDoPai;
        String name = node.item.getNome();
        quantities.put(name, quantities.getOrDefault(name, 0) + quantidadeDoItem);
        for (Node child : node.children) {
            acumularQuantidades(child, quantidadeDoItem, quantities);
        }
    }
}
