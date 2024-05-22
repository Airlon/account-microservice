package br.com.repassa.exception;

import br.com.backoffice_repassa_utils_lib.error.interfaces.RepassaUtilError;


public class AccountError implements RepassaUtilError {

    private static final String APP_PREFIX = "account";
    private String errorCode;
    private String errorMessage;


    public static final RepassaUtilError JSON_GERADO_NAO_VALIDO =
            new AccountError("001", "Erro ao processar o JSON.");

    public static final RepassaUtilError ENDPOINT_NAO_VALIDO =
            new AccountError("002", "O endpoint informado não é válido.");

    public static final RepassaUtilError GRUPO_NAO_ENCONTRADO =
            new AccountError("101", "Grupo não encontrado.");

    public static final RepassaUtilError NOME_GRUPO_NULO =
            new AccountError("102", "O nome do grupo não foi especificado.");

    public static final RepassaUtilError NOME_GRUPO_REPETIDO =
            new AccountError("103", "O nome especificado já encontra-se em uso.");

    public static final RepassaUtilError NOME_GRUPO_INVALIDO =
            new AccountError("104", "O nome precisa ter entre 1 e 255 caracteres.");

    public static final RepassaUtilError DESCRICAO_GRUPO_INVALIDO =
            new AccountError("105", "A descrição suporta somente até 255 caracteres.");

    public static final RepassaUtilError ERRO_CRIAR_GRUPO =
            new AccountError("106", "Erro ao criar grupo.");

    public static final RepassaUtilError MEMBROS_NAO_ENCONTRADOS =
            new AccountError("107", "Membros não encontrados.");

    public static final RepassaUtilError GRUPO_COM_CARACTERES_INVALIDOS =
            new AccountError("107", "Grupo não pode conter caracteres especiais!");

    public static final RepassaUtilError USUARIO_NAO_ENCONTRADO =
            new AccountError("201", "Usuário não encontrado.");

    public static final RepassaUtilError FILTRO_USUARIO_INVALIDO =
            new AccountError("202", "Filtro informado deve possuir no mínimo três caracteres.");

    public static final RepassaUtilError USUARIO_JA_NO_GRUPO =
            new AccountError("203", "O usuário já pertence ao grupo.");

    public static final RepassaUtilError USUARIO_NAO_ESTA_NO_GRUPO =
            new AccountError("203", "O usuário não pertence ao grupo.");

    public static final RepassaUtilError REGRA_NAO_ENCONTRADA =
            new AccountError("301", "Regra não encontrada.");

    public static final RepassaUtilError REGRA_NOME_REPETIDO =
            new AccountError("302", "Já existe uma regra com esse nome.");

    public static final RepassaUtilError CLIENTE_NAO_ENCONTRADO =
            new AccountError("401", "Cliente não encontrado.");

    public AccountError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getErrorCode() {
        return APP_PREFIX.concat("_").concat(errorCode);
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

}
