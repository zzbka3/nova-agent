package com.nova.agent.utils;

import com.nova.agent.entity.InputVar;

public class ConditionUtils {

    public static boolean contains(InputVar left, InputVar right) {
        if (left == null || right == null) return false;
        String lv = left.getVarValue() != null ? String.valueOf(left.getVarValue()) : "";
        String rv = right.getVarValue() != null ? String.valueOf(right.getVarValue()) : "";
        return lv.contains(rv);
    }

    public static boolean equal(InputVar left, InputVar right) {
        if (left == null || right == null) return false;
        Object lv = left.getVarValue();
        Object rv = right.getVarValue();
        if (lv == null && rv == null) return true;
        if (lv == null || rv == null) return false;
        return String.valueOf(lv).equals(String.valueOf(rv));
    }

    public static boolean gt(InputVar left, InputVar right) {
        try {
            double lv = left.getVarValue() != null ? Double.parseDouble(String.valueOf(left.getVarValue())) : 0;
            double rv = right.getVarValue() != null ? Double.parseDouble(String.valueOf(right.getVarValue())) : 0;
            return lv > rv;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean gte(InputVar left, InputVar right) {
        try {
            double lv = left.getVarValue() != null ? Double.parseDouble(String.valueOf(left.getVarValue())) : 0;
            double rv = right.getVarValue() != null ? Double.parseDouble(String.valueOf(right.getVarValue())) : 0;
            return lv >= rv;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean lengthGt(InputVar left, InputVar right) {
        int lv = getLength(left);
        int rv = right.getVarValue() != null ? Integer.parseInt(String.valueOf(right.getVarValue())) : 0;
        return lv > rv;
    }

    public static boolean lengthGte(InputVar left, InputVar right) {
        int lv = getLength(left);
        int rv = right.getVarValue() != null ? Integer.parseInt(String.valueOf(right.getVarValue())) : 0;
        return lv >= rv;
    }

    public static boolean empty(InputVar left) {
        if (left == null) return true;
        Object v = left.getVarValue();
        if (v == null) return true;
        String sv = String.valueOf(v);
        return sv.isEmpty() || "null".equals(sv) || "[]".equals(sv) || "{}".equals(sv);
    }

    public static boolean isTrue(InputVar left) {
        if (left == null) return false;
        Object v = left.getVarValue();
        if (v == null) return false;
        String sv = String.valueOf(v);
        return "true".equalsIgnoreCase(sv) || "1".equals(sv);
    }

    private static int getLength(InputVar var) {
        if (var == null || var.getVarValue() == null) return 0;
        String sv = String.valueOf(var.getVarValue());
        return sv.length();
    }
}
