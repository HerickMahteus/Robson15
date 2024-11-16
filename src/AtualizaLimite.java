import java.sql.*;
import java.util.Scanner;
public class AtualizaLimite {
    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement stmt = null;
        Scanner scanner = new Scanner(System.in);
        try {
// Configuração da conexão com o banco de dados
            String url = "jdbc:postgresql://localhost:5432/meubanco"; // Atualize com as credenciais do banco
                    conn = DriverManager.getConnection(url, "postgres", "senha123");
            conn.setAutoCommit(false); // Desativa o autocommit
// Solicita informações do cliente
            System.out.print("Digite o ID do cliente: ");
            int idCliente = scanner.nextInt();
            System.out.print("Digite o valor de aumento no limite: ");
            double aumento = scanner.nextDouble();
// Bloqueia o registro para atualização
            String lockQuery = "SELECT id FROM pessoas WHERE id = ? FOR UPDATE NOWAIT";
            PreparedStatement lockStmt = conn.prepareStatement(lockQuery);
            lockStmt.setInt(1, idCliente);
            try {
                ResultSet rs = lockStmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("Erro: Cliente não encontrado.");
                    return;
                }
            } catch (SQLException e) {
                if (e.getSQLState().equals("55P03")) {
                    System.out.println("Erro: O registro está sendo alterado por outro usuário.");
                    return;
                }
                throw e;
            }
// Atualiza o limite de crédito
            String updateQuery = "UPDATE pessoas SET limite_credito = limite_credito + ? WHERE id = ?";
            stmt = conn.prepareStatement(updateQuery);
            stmt.setDouble(1, aumento);
            stmt.setInt(2, idCliente);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.print("Confirma a atualização? (sim/não): ");
                String resposta = scanner.next();
                if ("sim".equalsIgnoreCase(resposta)) {
                    conn.commit();
                    System.out.println("Atualização confirmada com sucesso!");
                } else {
                    conn.rollback();
                    System.out.println("Atualização cancelada.");
                }
            } else {
                conn.rollback();
                System.out.println("Erro: Nenhum registro foi atualizado.");
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erro ao reverter transação: " + rollbackEx.getMessage());
            }
            System.out.println("Erro: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Erro ao fechar conexão: " + e.getMessage());
            }
            scanner.close();
        }
    }
}