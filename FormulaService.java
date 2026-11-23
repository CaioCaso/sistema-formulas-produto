import java.util.*;

public class FormulaService {

    // Monta uma árvore de fórmula a partir da lista de ItemFormula
    // estratégias:
    // - cria primeiro todos os nodes em mapa por nome
    // - encontra o item raiz (pai == null)
    // - constrói árvore ligando cada item ao seu pai com insertChild
    public static FormulaTree buildFormulaTree(List<ItemFormula> itens){
        FormulaTree tree = new FormulaTree();
        if(itens == null || itens.isEmpty()) return tree;

        // mapa de itens por nome
        Map<String, ItemFormula> mapa = new HashMap<String, ItemFormula>();
        for(ItemFormula it : itens) mapa.put(it.getNome(), it);

        // encontrar raiz(s) - item cujo pai == null
        ItemFormula raizItem = null;
        for(ItemFormula it : itens){
            if(it.getPai() == null){
                raizItem = it;
                break;
            }
        }
        if(raizItem == null){
            // fallback: pega primeiro
            raizItem = itens.get(0);
        }
        tree.setRoot(raizItem);

        // inserir os outros
        for(ItemFormula it : itens){
            if(it.getNome().equals(raizItem.getNome())) continue;
            String pai = it.getPai();
            if(pai == null){
                // se tiver outro sem pai, inserir sob raiz
                tree.insertChild(raizItem.getNome(), it);
            } else {
                // se pai presente no mapa, inserir; senão inserir sob raiz
                if(mapa.containsKey(pai)){
                    tree.insertChild(pai, it);
                } else {
                    tree.insertChild(raizItem.getNome(), it);
                }
            }
        }
        return tree;
    }

    // conversão para string (usar FormulaTree.toIndentedString())
    public static String treeToString(FormulaTree tree){
        if(tree == null) return "";
        return tree.toIndentedString();
    }

    public static double calcularTotal(FormulaTree tree){
        if(tree == null) return 0.0;
        return tree.calcularTotal();
    }

    public static Map<String,Integer> calcularQuantidades(FormulaTree tree, int quantidadeProdutos){
        if(tree == null) return new LinkedHashMap<String,Integer>();
        return tree.calcularQuantidades(quantidadeProdutos);
    }
}
