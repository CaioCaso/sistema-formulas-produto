import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ApiServer {

    private static final int DEFAULT_PORT = 8050;
    private static final String WEB_ROOT = "web";

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Database.inicializar();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Servir arquivos estáticos
        server.createContext("/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                URI uri = exchange.getRequestURI();
                String path = uri.getPath();
                if (path.equals("/")) path = "/index.html";
                Path filePath = Paths.get(WEB_ROOT + path).normalize();
                if (!filePath.startsWith(Paths.get(WEB_ROOT))) {
                    exchange.sendResponseHeaders(403, -1);
                    return;
                }
                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    byte[] bytes = Files.readAllBytes(filePath);
                    String type = Files.probeContentType(filePath);
                    if (type == null) type = "text/plain";
                    exchange.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    String msg = "404 Not Found";
                    exchange.sendResponseHeaders(404, msg.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(msg.getBytes());
                    }
                }
            }
        });

        // Adicionar item
        server.createContext("/api/add", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Map<String, String> p = parseForm(body);
            String produto = p.get("produto");
            String nome = p.get("nome");
            String pai = p.get("pai");
            int qtd = Integer.parseInt(p.getOrDefault("quantidade", "1"));
            double valor = Double.parseDouble(p.getOrDefault("valor", "0.0"));

            Database.inserir(nome, (pai == null || pai.isEmpty()) ? null : pai, qtd, valor, produto);
            writeJson(exchange, "{\"status\":\"ok\"}");
        });

        // Listar produtos
        server.createContext("/api/produtos", exchange -> {
            List<String> ps = Database.listarProdutos();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < ps.size(); i++) {
                sb.append("\"").append(escape(ps.get(i))).append("\"");
                if (i < ps.size() - 1) sb.append(",");
            }
            sb.append("]");
            writeJson(exchange, sb.toString());
        });

        // Carregar fórmula
        server.createContext("/api/carregar", exchange -> {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            String produto = q.get("produto");
            if (produto == null) {
                writeJson(exchange, "{\"error\":\"produto param required\"}");
                return;
            }
            List<ItemFormula> itens = Database.carregar(produto);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < itens.size(); i++) {
                ItemFormula it = itens.get(i);
                sb.append("{");
                sb.append("\"nome\":\"").append(escape(it.getNome())).append("\",");
                if (it.getPai() == null)
                    sb.append("\"pai\":null,");
                else
                    sb.append("\"pai\":\"").append(escape(it.getPai())).append("\",");
                sb.append("\"quantidade\":").append(it.getQuantidade()).append(",");
                sb.append("\"valor\":").append(String.format(Locale.US, "%.2f", it.getValorUnitario()));
                sb.append("}");
                if (i < itens.size() - 1) sb.append(",");
            }
            sb.append("]");
            writeJson(exchange, sb.toString());
        });

        // Explodir
        server.createContext("/api/explodir", exchange -> {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            String produto = q.get("produto");
            if (produto == null) {
                writeJson(exchange, "{\"error\":\"produto param required\"}");
                return;
            }
            List<ItemFormula> itens = Database.carregar(produto);
            FormulaTree tree = FormulaService.buildFormulaTree(itens);
            String texto = FormulaService.treeToString(tree);
            writeJson(exchange, "{\"text\":\"" + escape(texto) + "\"}");
        });

        // Calcular custo
        server.createContext("/api/calcular", exchange -> {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            String produto = q.get("produto");
            int qtd = Integer.parseInt(q.getOrDefault("qtd", "1"));

            if (produto == null || produto.isEmpty()) {
                writeJson(exchange, "{\"error\":\"produto param required\"}");
                return;
            }

            List<ItemFormula> itens = Database.carregar(produto);
            FormulaTree tree = FormulaService.buildFormulaTree(itens);

            // Se não houver itens ou árvore inválida
            if (tree == null) {
                writeJson(exchange, "{\"error\":\"produto não encontrado ou fórmula inválida\"}");
                return;
            }

            double totalUnitario = FormulaService.calcularTotal(tree);
            Map<String, Integer> comps = FormulaService.calcularQuantidades(tree, qtd);

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"total_unitario\":").append(String.format(Locale.US, "%.2f", totalUnitario)).append(",");
            sb.append("\"total_para_qtd\":").append(String.format(Locale.US, "%.2f", totalUnitario * qtd)).append(",");
            sb.append("\"componentes\":{");

            int i = 0;
            for (Map.Entry<String, Integer> e : comps.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(escape(e.getKey())).append("\":").append(e.getValue());
            }

            sb.append("}}");
            writeJson(exchange, sb.toString());
        });


        // Atualizar valor
        server.createContext("/api/atualizar", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Map<String, String> p = parseForm(body);
            String nome = p.get("nome");
            String produto = p.get("produto");
            double valor = Double.parseDouble(p.getOrDefault("valor", "0.0"));
            if (Database.hasChildren(produto, nome) && valor > 0.0) {
                Database.atualizarValor(nome, 0.0, produto);
                writeJson(exchange, "{\"status\":\"ok\",\"info\":\"componente é pai; valor definido como 0\"}");
                return;
            }
            Database.atualizarValor(nome, valor, produto);
            writeJson(exchange, "{\"status\":\"ok\"}");
        });

        // Deletar produto
        server.createContext("/api/deletar", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Map<String, String> p = parseForm(body);
            String produto = p.get("produto");
            Database.deletar(produto);
            writeJson(exchange, "{\"status\":\"ok\"}");
        });

        // hasChildren
        server.createContext("/api/hasChildren", exchange -> {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            String produto = q.get("produto");
            String nome = q.get("nome");
            boolean r = Database.hasChildren(produto, nome);
            writeJson(exchange, "{\"hasChildren\":" + (r ? "true" : "false") + "}");
        });

        System.out.println("Servidor iniciado em http://localhost:" + port);
        server.start();
    }

    // utilitários
    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int r;
        while ((r = in.read(buf)) != -1) baos.write(buf, 0, r);
        return baos.toByteArray();
    }

    private static void writeJson(HttpExchange ex, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;
        String[] parts = body.split("&");
        for (String p : parts) {
            String[] kv = p.split("=", 2);
            try {
                String k = URLDecoder.decode(kv[0], "UTF-8");
                String v = kv.length > 1 ? URLDecoder.decode(kv[1], "UTF-8") : "";
                map.put(k, v);
            } catch (Exception ignored) {}
        }
        return map;
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> map = new HashMap<>();
        if (q == null || q.isEmpty()) return map;
        String[] parts = q.split("&");
        for (String p : parts) {
            String[] kv = p.split("=", 2);
            try {
                String k = URLDecoder.decode(kv[0], "UTF-8");
                String v = kv.length > 1 ? URLDecoder.decode(kv[1], "UTF-8") : "";
                map.put(k, v);
            } catch (Exception ignored) {}
        }
        return map;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
