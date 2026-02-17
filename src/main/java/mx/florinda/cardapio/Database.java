package mx.florinda.cardapio;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class Database {

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/cardapio?useSSL=false&serverTimezone=UTC";
        return DriverManager.getConnection(url, "root", "senha123");
    }

    public List<ItemCardapio> listaItensCardapio() {
        List<ItemCardapio> itens = new ArrayList<>();
        String sql = "SELECT * FROM item_cardapio";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ItemCardapio item = new ItemCardapio(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        ItemCardapio.CategoriaCardapio.valueOf(rs.getString("categoria")),
                        rs.getBigDecimal("preco"),
                        rs.getBigDecimal("preco_promocional")
                );
                itens.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itens;
    }

    public Optional<ItemCardapio> itemCardapioPorId(Long id) {
        String sql = "SELECT * FROM item_cardapio WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new ItemCardapio(
                            rs.getLong("id"),
                            rs.getString("nome"),
                            rs.getString("descricao"),
                            ItemCardapio.CategoriaCardapio.valueOf(rs.getString("categoria")),
                            rs.getBigDecimal("preco"),
                            rs.getBigDecimal("preco_promocional")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    public boolean alteraPrecoItemCardapio(Long id, BigDecimal novoPreco) {
        String sql = "UPDATE item_cardapio SET preco = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, novoPreco);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void rastroAuditoriaPrecos() {
        // Como o banco é relacional e não temos uma tabela de auditoria ainda, 
        // vamos apenas imprimir uma mensagem para manter a compatibilidade com a Main.
        System.out.println("\nAuditoria de preços via Banco de Dados (consulte logs do MySQL para histórico completo).");
    }

    public boolean removeItemCardapio(Long id) {
        String sql = "DELETE FROM item_cardapio WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void adicionaItemCardapio(ItemCardapio item) {
        String sql = "INSERT INTO item_cardapio (id, nome, descricao, categoria, preco, preco_promocional) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, item.id());
            stmt.setString(2, item.nome());
            stmt.setString(3, item.descricao());
            stmt.setString(4, item.categoria().name());
            stmt.setBigDecimal(5, item.preco());
            stmt.setBigDecimal(6, item.precoPromocional());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int totalItensCardapio() {
        String sql = "SELECT COUNT(*) FROM item_cardapio";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}