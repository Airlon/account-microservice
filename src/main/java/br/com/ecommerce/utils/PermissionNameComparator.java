package br.com.repassa.utils;

import br.com.repassa.dto.PermissionDTO;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PermissionNameComparator implements Comparator<PermissionDTO> {

    private static final Map<String, Integer> SUBCATEGORY_PRIORITY_MAP = new HashMap<>();

    static {
        SUBCATEGORY_PRIORITY_MAP.put("TRIAGEM.TRIAR_SACOLA", 0);
        SUBCATEGORY_PRIORITY_MAP.put("TRIAGEM.CONSULTAR_SACOLAS", 1);
        SUBCATEGORY_PRIORITY_MAP.put("TRIAGEM.IMPRIMIR_ETIQUETA", 2);

        SUBCATEGORY_PRIORITY_MAP.put("ARMAZENAR PRODUTOS.CRIAR_ORDEM_DE_ENTRADA", 0);
        SUBCATEGORY_PRIORITY_MAP.put("ARMAZENAR PRODUTOS.EDITAR_LACRE_ORDEM_DE_ENTRADA", 1);
        SUBCATEGORY_PRIORITY_MAP.put("ARMAZENAR PRODUTOS.CONSULTAR_ORDENS_DE_ENTRADA", 2);
    }

    @Override
    public int compare(PermissionDTO c1, PermissionDTO c2) {
        int priority1 = SUBCATEGORY_PRIORITY_MAP.getOrDefault(c1.getName(), Integer.MAX_VALUE);
        int priority2 = SUBCATEGORY_PRIORITY_MAP.getOrDefault(c2.getName(), Integer.MAX_VALUE);
        int categoryComparison = Integer.compare(priority1, priority2);

        if (categoryComparison != 0) {
            return categoryComparison;
        }

        return -1;
    }
}
