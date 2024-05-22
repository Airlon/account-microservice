package br.com.repassa.utils;

import br.com.repassa.dto.CategoryPermissionDTO;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PermissionComparator implements Comparator<CategoryPermissionDTO> {
        private static final Map<String, Integer> CATEGORY_PRIORITY_MAP = new HashMap<>();

        static {
            CATEGORY_PRIORITY_MAP.put("RECEBIMENTO", 0);
            CATEGORY_PRIORITY_MAP.put("TRIAGEM", 1);
            CATEGORY_PRIORITY_MAP.put("SACOLAS REPROVADAS", 2);
            CATEGORY_PRIORITY_MAP.put("FOTOGRAFIA", 3);
            CATEGORY_PRIORITY_MAP.put("CADASTRO DE PRODUTOS", 4);
            CATEGORY_PRIORITY_MAP.put("ARMAZENAR PRODUTOS", 5);
            CATEGORY_PRIORITY_MAP.put("HISTÓRICO DE PROCESSAMENTO DA SACOLA", 6);
            CATEGORY_PRIORITY_MAP.put("TRANSACIONAIS", 7);
        }

        @Override
        public int compare(CategoryPermissionDTO c1, CategoryPermissionDTO c2) {
            int priority1 = CATEGORY_PRIORITY_MAP.getOrDefault(c1.getCategory(), Integer.MAX_VALUE);
            int priority2 = CATEGORY_PRIORITY_MAP.getOrDefault(c2.getCategory(), Integer.MAX_VALUE);
            int categoryComparison = Integer.compare(priority1, priority2);

            if (categoryComparison != 0) {
                return categoryComparison;
            }


            return Integer.compare(c1.getPermissions().size(), c2.getPermissions().size());
        }
    }
