import java.sql.*;
import java.util.*;

public class Database {
    private static final String URL = "jdbc:sqlite:produto.db";

    public static void inicializar(){
        Connection conn = null;
        Statement st = null;
        try{
            conn = DriverManager.getConnection(URL);
            st = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS formula (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "nome TEXT NOT NULL," +
                         "pai TEXT," +
                         "quantidade INTEGER NOT NULL," +
                         "valor REAL NOT NULL," +
                         "produto_raiz TEXT NOT NULL" +
                         ");";
            st.execute(sql);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{ if(st!=null) st.close(); } catch(Exception e){}
            try{ if(conn!=null) conn.close(); } catch(Exception e){}
        }
    }

public static void inserir(String nome, String pai, int qtd, double valor, String produtoRaiz){
    try (Connection conn = DriverManager.getConnection(URL)) {
        // Inserir o novo item
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO formula (nome,pai,quantidade,valor,produto_raiz) VALUES (?,?,?,?,?)")) {
            ps.setString(1, nome);
            ps.setObject(2, (pai == null || pai.trim().isEmpty()) ? null : pai);
            ps.setInt(3, qtd);
            ps.setDouble(4, valor);
            ps.setString(5, produtoRaiz);
            ps.executeUpdate();
        }

        // Se o item tem pai, forçar o valor do pai a 0 (na mesma conexão)
        if (pai != null && !pai.trim().isEmpty()) {
            try (PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE formula SET valor = 0 WHERE nome = ? AND produto_raiz = ?")) {
                ps2.setString(1, pai);
                ps2.setString(2, produtoRaiz);
                ps2.executeUpdate();
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    public static void atualizarValor(String nome, double novoValor, String produtoRaiz){
        Connection conn = null;
        PreparedStatement ps = null;
        try{
            conn = DriverManager.getConnection(URL);
            String sql = "UPDATE formula SET valor=? WHERE nome=? AND produto_raiz=?";
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, novoValor);
            ps.setString(2, nome);
            ps.setString(3, produtoRaiz);
            ps.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{ if(ps!=null) ps.close(); }catch(Exception e){}
            try{ if(conn!=null) conn.close(); }catch(Exception e){}
        }
    }

    public static List<ItemFormula> carregar(String produtoRaiz){
        List<ItemFormula> lista = new ArrayList<ItemFormula>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(URL);
            String sql = "SELECT nome,pai,quantidade,valor FROM formula WHERE produto_raiz = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, produtoRaiz);
            rs = ps.executeQuery();
            while(rs.next()){
                String nome = rs.getString("nome");
                String pai = rs.getString("pai");
                int qtd = rs.getInt("quantidade");
                double valor = rs.getDouble("valor");
                lista.add(new ItemFormula(nome, pai, qtd, valor));
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{ if(rs!=null) rs.close(); }catch(Exception e){}
            try{ if(ps!=null) ps.close(); }catch(Exception e){}
            try{ if(conn!=null) conn.close(); }catch(Exception e){}
        }
        return lista;
    }

    public static List<String> listarProdutos(){
        List<String> lista = new ArrayList<String>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(URL);
            st = conn.createStatement();
            rs = st.executeQuery("SELECT DISTINCT produto_raiz FROM formula");
            while(rs.next()){
                lista.add(rs.getString(1));
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{ if(rs!=null) rs.close(); }catch(Exception e){}
            try{ if(st!=null) st.close(); }catch(Exception e){}
            try{ if(conn!=null) conn.close(); }catch(Exception e){}
        }
        return lista;
    }

    public static void deletar(String produtoRaiz){
        Connection conn = null;
        PreparedStatement ps = null;
        try{
            conn = DriverManager.getConnection(URL);
            ps = conn.prepareStatement("DELETE FROM formula WHERE produto_raiz = ?");
            ps.setString(1, produtoRaiz);
            ps.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{ if(ps!=null) ps.close(); }catch(Exception e){}
            try{ if(conn!=null) conn.close(); }catch(Exception e){}
        }
    }

    // retorna true se o componente nome tem filhos (existem linhas com pai = nome)
    public static boolean hasChildren(String produtoRaiz, String nome){
        boolean result = false;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(URL);
            ps = conn.prepareStatement("SELECT COUNT(1) FROM formula WHERE produto_raiz = ? AND pai = ?");
            ps.setString(1, produtoRaiz);
            ps.setString(2, nome);
            rs = ps.executeQuery();
            if(rs.next()){
                result = rs.getInt(1) > 0;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{ if(rs!=null) rs.close(); }catch(Exception e){}
            try{ if(ps!=null) ps.close(); }catch(Exception e){}
            try{ if(conn!=null) conn.close(); }catch(Exception e){}
        }
        return result;
    }
}
