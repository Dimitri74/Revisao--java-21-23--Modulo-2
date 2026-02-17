package mx.florinda.cardapio;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServidorItensCardapio {

    public static void main(String[] args) throws IOException {
        Database database = new Database();
        Gson gson = new Gson();

        InetSocketAddress inetSocketAddress = new InetSocketAddress(8000);
        HttpServer httpServer = HttpServer.create(inetSocketAddress, 0);

        httpServer.createContext("/cardapio", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.add("Content-type", "application/json; charset=UTF-8");

                if ("GET".equalsIgnoreCase(method)) {
                    List<ItemCardapio> itens = database.listaItensCardapio();
                    String json = gson.toJson(itens);
                    byte[] bytes = json.getBytes("UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } 
                else if ("POST".equalsIgnoreCase(method)) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
                    ItemCardapio novoItem = gson.fromJson(body, ItemCardapio.class);
                    database.adicionaItemCardapio(novoItem);
                    exchange.sendResponseHeaders(201, -1); // Created
                } 
                else if ("PUT".equalsIgnoreCase(method)) {
                    // Espera algo como /cardapio?id=1&novoPreco=5.99
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null) {
                        Map<String, String> params = parseQuery(query);
                        Long id = Long.parseLong(params.get("id"));
                        BigDecimal novoPreco = new BigDecimal(params.get("novoPreco"));
                        boolean atualizado = database.alteraPrecoItemCardapio(id, novoPreco);
                        exchange.sendResponseHeaders(atualizado ? 200 : 404, -1);
                    } else {
                        exchange.sendResponseHeaders(400, -1);
                    }
                } 
                else if ("DELETE".equalsIgnoreCase(method)) {
                    // Espera /cardapio?id=1
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null) {
                        Map<String, String> params = parseQuery(query);
                        Long id = Long.parseLong(params.get("id"));
                        boolean removido = database.removeItemCardapio(id);
                        exchange.sendResponseHeaders(removido ? 200 : 404, -1);
                    } else {
                        exchange.sendResponseHeaders(400, -1);
                    }
                } 
                else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }
        });

        System.out.println("Servidor HTTP rodando na porta 8000...");
        System.out.println("Endpoints disponíveis:");
        System.out.println("GET    http://localhost:8000/cardapio (Listar)");
        System.out.println("POST   http://localhost:8000/cardapio (Adicionar - Body JSON)");
        System.out.println("PUT    http://localhost:8000/cardapio?id=X&novoPreco=Y (Alterar Preço)");
        System.out.println("DELETE http://localhost:8000/cardapio?id=X (Remover)");
        httpServer.start();
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new java.util.HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }
        }
        return result;
    }
}