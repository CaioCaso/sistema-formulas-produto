public class ItemFormula {
    private String nome;
    private String pai; // null = raiz
    private int quantidade;
    private double valorUnitario;

    public ItemFormula(String nome, String pai, int quantidade, double valorUnitario){
        this.nome = nome;
        this.pai = (pai == null || pai.trim().isEmpty()) ? null : pai;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public String getNome(){ return nome; }
    public String getPai(){ return pai; }
    public int getQuantidade(){ return quantidade; }
    public double getValorUnitario(){ return valorUnitario; }
    public void setValorUnitario(double v){ this.valorUnitario = v; }

    @Override
    public String toString(){
        return nome + " (Qtd: " + quantidade + ", R$" + String.format("%.2f", valorUnitario) + ")";
    }
}
