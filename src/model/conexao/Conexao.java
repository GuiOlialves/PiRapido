
package model.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Conexao {

    private static final String URL_SERVIDOR = "jdbc:mysql://localhost:3306/";
    private static final String URL = "jdbc:mysql://localhost:3306/papelogistic";
    private static final String USUARIO = "root";
    private static final String SENHA = "";
    private static final String[] SENHAS_TENTATIVA = {SENHA, "root", "1234", "admin"};

    private static String senhaAtiva = SENHA;
    private static boolean bancoInicializado = false;

    private static final String[] SQL_ESTRUTURA = {
        "CREATE DATABASE IF NOT EXISTS papelogistic DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci",
        "USE papelogistic",
        "CREATE TABLE IF NOT EXISTS cliente ("
            + "CodCliente INT NOT NULL AUTO_INCREMENT, "
            + "IdPessoa VARCHAR(100) NOT NULL, "
            + "nomeCliente VARCHAR(150) NOT NULL, "
            + "emailCliente VARCHAR(150) DEFAULT NULL, "
            + "telefoneCliente VARCHAR(20) DEFAULT NULL, "
            + "cep CHAR(8) DEFAULT NULL, "
            + "PRIMARY KEY (CodCliente)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci",
        "CREATE TABLE IF NOT EXISTS produto ("
            + "CodProduto INT NOT NULL AUTO_INCREMENT, "
            + "nomeProduto VARCHAR(150) NOT NULL, "
            + "tipo VARCHAR(100) NOT NULL, "
            + "valor DECIMAL(10,2) NOT NULL, "
            + "PRIMARY KEY (CodProduto)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci",
        "CREATE TABLE IF NOT EXISTS usuario ("
            + "login_simples VARCHAR(50) NOT NULL, "
            + "senha VARCHAR(255) NOT NULL, "
            + "nome VARCHAR(100) NOT NULL, "
            + "telefoneUsuario VARCHAR(20) DEFAULT NULL, "
            + "emailUsuario VARCHAR(150) NOT NULL, "
            + "PRIMARY KEY (login_simples), "
            + "UNIQUE KEY emailUsuario (emailUsuario)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci",
        "CREATE TABLE IF NOT EXISTS estoque ("
            + "CodProduto INT NOT NULL, "
            + "quantidade INT NOT NULL DEFAULT 0, "
            + "PRIMARY KEY (CodProduto), "
            + "CONSTRAINT FK_Estoque_Produto FOREIGN KEY (CodProduto) "
            + "REFERENCES produto (CodProduto) ON DELETE CASCADE ON UPDATE CASCADE"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci",
        "CREATE TABLE IF NOT EXISTS pedido ("
            + "CodPedido INT NOT NULL AUTO_INCREMENT, "
            + "CodCliente INT NOT NULL, "
            + "dataPedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(), "
            + "PRIMARY KEY (CodPedido), "
            + "KEY IX_Pedido_Cliente (CodCliente), "
            + "CONSTRAINT FK_Pedido_Cliente FOREIGN KEY (CodCliente) "
            + "REFERENCES cliente (CodCliente) ON UPDATE CASCADE"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci",
        "CREATE TABLE IF NOT EXISTS item_pedido ("
            + "CodPedido INT NOT NULL, "
            + "CodProduto INT NOT NULL, "
            + "quantidade INT NOT NULL, "
            + "ValorUnitario DECIMAL(10,2) NOT NULL, "
            + "PRIMARY KEY (CodPedido, CodProduto), "
            + "KEY IX_ItemPedido_Produto (CodProduto), "
            + "CONSTRAINT FK_ItemPedido_Pedido FOREIGN KEY (CodPedido) "
            + "REFERENCES pedido (CodPedido) ON DELETE CASCADE ON UPDATE CASCADE, "
            + "CONSTRAINT FK_ItemPedido_Produto FOREIGN KEY (CodProduto) "
            + "REFERENCES produto (CodProduto) ON UPDATE CASCADE"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci",
        "CREATE TABLE IF NOT EXISTS financeiro ("
            + "CodFinanceiro INT NOT NULL AUTO_INCREMENT, "
            + "Caixa DECIMAL(10,2) NOT NULL DEFAULT 0.00, "
            + "ValorEntrada DECIMAL(10,2) NOT NULL DEFAULT 0.00, "
            + "ValorSaida DECIMAL(10,2) NOT NULL DEFAULT 0.00, "
            + "CodPedido INT DEFAULT NULL, "
            + "dataMovimentacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(), "
            + "PRIMARY KEY (CodFinanceiro), "
            + "KEY IX_Financeiro_Pedido (CodPedido), "
            + "CONSTRAINT FK_Financeiro_Pedido FOREIGN KEY (CodPedido) "
            + "REFERENCES pedido (CodPedido) ON DELETE SET NULL ON UPDATE CASCADE"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    };

    private static final String[][] COLUNAS_OBRIGATORIAS = {
        {"cliente", "CodCliente", "INT NOT NULL AUTO_INCREMENT"},
        {"cliente", "IdPessoa", "VARCHAR(100) NOT NULL DEFAULT ''"},
        {"cliente", "nomeCliente", "VARCHAR(150) NOT NULL DEFAULT ''"},
        {"cliente", "emailCliente", "VARCHAR(150) DEFAULT NULL"},
        {"cliente", "telefoneCliente", "VARCHAR(20) DEFAULT NULL"},
        {"cliente", "cep", "CHAR(8) DEFAULT NULL"},
        {"produto", "CodProduto", "INT NOT NULL AUTO_INCREMENT"},
        {"produto", "nomeProduto", "VARCHAR(150) NOT NULL DEFAULT '' AFTER CodProduto"},
        {"produto", "tipo", "VARCHAR(100) NOT NULL DEFAULT ''"},
        {"produto", "valor", "DECIMAL(10,2) NOT NULL DEFAULT 0.00"},
        {"usuario", "login_simples", "VARCHAR(50) NOT NULL"},
        {"usuario", "senha", "VARCHAR(255) NOT NULL DEFAULT ''"},
        {"usuario", "nome", "VARCHAR(100) NOT NULL DEFAULT ''"},
        {"usuario", "telefoneUsuario", "VARCHAR(20) DEFAULT NULL"},
        {"usuario", "emailUsuario", "VARCHAR(150) NOT NULL DEFAULT ''"},
        {"estoque", "CodProduto", "INT NOT NULL"},
        {"estoque", "quantidade", "INT NOT NULL DEFAULT 0"},
        {"pedido", "CodPedido", "INT NOT NULL AUTO_INCREMENT"},
        {"pedido", "CodCliente", "INT NOT NULL"},
        {"pedido", "dataPedido", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()"},
        {"item_pedido", "CodPedido", "INT NOT NULL"},
        {"item_pedido", "CodProduto", "INT NOT NULL"},
        {"item_pedido", "quantidade", "INT NOT NULL DEFAULT 0"},
        {"item_pedido", "ValorUnitario", "DECIMAL(10,2) NOT NULL DEFAULT 0.00"},
        {"financeiro", "CodFinanceiro", "INT NOT NULL AUTO_INCREMENT"},
        {"financeiro", "Caixa", "DECIMAL(10,2) NOT NULL DEFAULT 0.00"},
        {"financeiro", "ValorEntrada", "DECIMAL(10,2) NOT NULL DEFAULT 0.00"},
        {"financeiro", "ValorSaida", "DECIMAL(10,2) NOT NULL DEFAULT 0.00"},
        {"financeiro", "CodPedido", "INT DEFAULT NULL"},
        {"financeiro", "dataMovimentacao", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()"}
    };

    public static Connection conectar() throws SQLException {
        if (!bancoInicializado) {
            inicializarBanco();
        }
        return DriverManager.getConnection(URL, USUARIO, senhaAtiva);
    }

    private static synchronized void inicializarBanco() throws SQLException {
        if (bancoInicializado) {
            return;
        }
        SQLException ultimoErro = null;
        for (String tentativa : SENHAS_TENTATIVA) {
            try (Connection conexaoServidor = DriverManager.getConnection(URL_SERVIDOR, USUARIO, tentativa);
                 Statement stmt = conexaoServidor.createStatement()) {
                for (String sql : SQL_ESTRUTURA) {
                    stmt.executeUpdate(sql);
                }
                atualizarEstrutura(stmt);
                senhaAtiva = tentativa;
                bancoInicializado = true;
                return;
            } catch (SQLException erro) {
                ultimoErro = erro;
                // Código 1045 = Access denied (tenta a próxima senha padrão).
                if (erro.getErrorCode() != 1045) {
                    break;
                }
            }
        }
        throw ultimoErro;
    }

    private static void atualizarEstrutura(Statement stmt) throws SQLException {
        Map<String, Set<String>> colunasExistentes = new HashMap<>();
        try (ResultSet rs = stmt.executeQuery(
                "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = 'papelogistic'")) {
            while (rs.next()) {
                String tabela = rs.getString("TABLE_NAME").toLowerCase(Locale.ROOT);
                String coluna = rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT);
                colunasExistentes.computeIfAbsent(tabela, k -> new HashSet<>()).add(coluna);
            }
        }

        Map<String, Set<String>> colunasEsperadas = new HashMap<>();
        boolean adicionouNomeProduto = false;
        for (String[] definicao : COLUNAS_OBRIGATORIAS) {
            String tabela = definicao[0].toLowerCase(Locale.ROOT);
            String coluna = definicao[1];
            String colunaNormalizada = coluna.toLowerCase(Locale.ROOT);
            colunasEsperadas.computeIfAbsent(tabela, k -> new HashSet<>()).add(colunaNormalizada);

            Set<String> existentes = colunasExistentes.get(tabela);
            if (existentes != null && !existentes.contains(colunaNormalizada)) {
                stmt.executeUpdate("ALTER TABLE " + definicao[0] + " ADD COLUMN " + coluna + " " + definicao[2]);
                existentes.add(colunaNormalizada);
                if ("produto".equals(tabela) && "nomeproduto".equals(colunaNormalizada)) {
                    adicionouNomeProduto = true;
                }
            }
        }

        Set<String> colsProduto = colunasExistentes.get("produto");
        if (adicionouNomeProduto) {
            if (colsProduto != null && colsProduto.contains("nome")) {
                stmt.executeUpdate("UPDATE produto SET nomeProduto = nome WHERE nomeProduto = '' AND nome IS NOT NULL AND nome <> ''");
            }
            stmt.executeUpdate("UPDATE produto SET nomeProduto = tipo WHERE nomeProduto = '' AND tipo <> ''");
        }
        if (colsProduto != null && colsProduto.contains("quantidade")) {
            stmt.executeUpdate("INSERT IGNORE INTO estoque (CodProduto, quantidade) SELECT CodProduto, COALESCE(quantidade, 0) FROM produto");
        }

        List<String> alteracoesLegadas = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery(
                "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA, COLUMN_KEY "
                + "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'papelogistic'")) {
            while (rs.next()) {
                String tabela = rs.getString("TABLE_NAME");
                String tabelaNorm = tabela.toLowerCase(Locale.ROOT);
                String coluna = rs.getString("COLUMN_NAME");
                String colunaNorm = coluna.toLowerCase(Locale.ROOT);
                Set<String> esperadas = colunasEsperadas.get(tabelaNorm);
                if (esperadas == null || esperadas.contains(colunaNorm)) {
                    continue;
                }
                String isNullable = rs.getString("IS_NULLABLE");
                String padrao = rs.getString("COLUMN_DEFAULT");
                String extra = rs.getString("EXTRA");
                String chave = rs.getString("COLUMN_KEY");
                boolean autoInc = extra != null && extra.toLowerCase(Locale.ROOT).contains("auto_increment");
                boolean primaria = chave != null && "PRI".equalsIgnoreCase(chave);
                if ("NO".equalsIgnoreCase(isNullable) && padrao == null && !autoInc && !primaria) {
                    String tipoColuna = rs.getString("COLUMN_TYPE");
                    alteracoesLegadas.add("ALTER TABLE `" + tabela + "` MODIFY COLUMN `" + coluna + "` " + tipoColuna + " NULL DEFAULT NULL");
                }
            }
        }
        for (String sql : alteracoesLegadas) {
            stmt.executeUpdate(sql);
        }
    }

    public static void main(String[] args) {
        try (Connection conexao = conectar()) {
            System.out.println("Conexão realizada com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao conectar com o banco de dados: " + e.getMessage());
        }
    }
}
