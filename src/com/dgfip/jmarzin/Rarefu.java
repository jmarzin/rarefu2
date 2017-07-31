package com.dgfip.jmarzin;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Rarefu {

    private static Dictionary<String, String> dictionnaire = new Hashtable <>();
    private static String lignePrecedente = "";
    private static LecteurPdfParLigne lecteurPdfParLigne;

    private static String abbrev(String etat) {
        if (etat.contains("PRESCRITES")) {
            return "PRESCRITES";
        } else if (etat.contains("COMPROMIS")) {
            return "COMPROMIS";
        } else {
            return "PROC COLL";
        }
    }
    private static boolean majDictionnaire(Pattern pattern, String ligne, boolean flush, String[] items) {
        Matcher matcher = pattern.matcher(ligne);
        if(matcher.matches()) {
            int i = 1;
            for (String item: items) {
                dictionnaire.put(item, matcher.group(i++));
            }
            if (flush) {
                String ligneAEcrire = dictionnaire.get("role") + "|" +
                        dictionnaire.get("mer") + "|" +
                        dictionnaire.get("solde") + "|" +
                        dictionnaire.get("compte") + "|" +
                        dictionnaire.get("nom") + "|" +
                        dictionnaire.get("exercice") + "|" +
                        abbrev(dictionnaire.get("etat")) + "|" +
                        dictionnaire.get("poste");
                if (!ligneAEcrire.equals(lignePrecedente)) {
                    lecteurPdfParLigne.ecrit(ligneAEcrire + "|" + dictionnaire.get("page") + "\n");
                    lignePrecedente = ligneAEcrire;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    public static void main(String[] args) {
        lecteurPdfParLigne = new LecteurPdfParLigne();
        dictionnaire.put("poste","");
        dictionnaire.put("page", "");
        dictionnaire.put("etat", "");
        dictionnaire.put("exercice","");
        dictionnaire.put("compte", "");
        dictionnaire.put("nom", "");
        dictionnaire.put("role", "");
        dictionnaire.put("mer", "");
        dictionnaire.put("solde", "");
        Pattern[] patterns = {Pattern.compile(".*POSTE : (\\d{6}) +PAGE : (\\d+).*"),
                Pattern.compile(".*(COTES APPAREMMENT PRESCRITES|COTES DONT LE RECOUVREMENT PARAIT COMPROMIS|PROCEDURES {2}COLLECTIVES {2}EN COURS).*"),
                Pattern.compile(".*EXERCICE (\\d{4}).*"),
                Pattern.compile(".*NUMERO COMPTE : (\\d+) +NOM : (.*?) {2}.*"),
                Pattern.compile(" *(\\d+) +(\\d\\d/\\d\\d/\\d{4}) .*?(\\d+,\\d\\d).*")};
        String[][]items = {{"poste","page"},{"etat"},{"exercice"},{"compte","nom"},{"role","mer","solde"}};
        while (lecteurPdfParLigne.hasNext()) {
            String ligne = lecteurPdfParLigne.next();
            int iitem = 0;
            for (Pattern pattern : patterns) {
                if (majDictionnaire(pattern, ligne, iitem == items.length - 1, items[iitem++])) {
                    break;
                }
            }
        }
        lecteurPdfParLigne.close();
    }
}