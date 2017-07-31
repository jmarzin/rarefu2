package com.dgfip.jmarzin;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;


public class LecteurPdfParLigne implements Iterator {
    private PdfReader lecteurPdf;
    private String[] lignes = new String[1];
    private int page = 0;
    private int ligne = 1;
    private FileOutputStream fout;
    private ProgressMonitor progressMonitor;

    void ecrit(String s) {
        try {
            fout.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void close() {
        try {
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lecteurPdf.close();
        progressMonitor.close();
    }

    private String getChaine(int page) {
        String chaine = "";
        try {
            chaine = PdfTextExtractor.getTextFromPage(this.lecteurPdf, page);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chaine;
    }

    LecteurPdfParLigne() {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Fichier RAREFU", "pdf");
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle("Sélectionner le fichier RAREFU à traiter");
        int returnVal = fileChooser.showOpenDialog(null);
        if(returnVal != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        String path = fileChooser.getSelectedFile().getAbsolutePath();
        try {
            lecteurPdf = new PdfReader(path);
            this.fout = new FileOutputStream(path.replaceAll("\\.pdf", ".csv"));
            ecrit("ROLE|MER|SOLDE|COMPTE|NOM|EXERCICE|ETAT|POSTE|PAGE|NOTES POSTE|NOTES DIRECTION\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        progressMonitor = new ProgressMonitor(null,
                "Traitement du fichier " + fileChooser.getSelectedFile().getName(),
                "", 0, 100);
    }

    @Override
    public boolean hasNext() {
        return !(ligne >= lignes.length && page >= this.lecteurPdf.getNumberOfPages());
    }

    @Override
    public String next() {
        if (ligne >= lignes.length) {
            if (page >= this.lecteurPdf.getNumberOfPages()) {
                return null;
            } else {
                this.lignes = getChaine(++page).split("\n");
                progressMonitor.setProgress(page * 100 / lecteurPdf.getNumberOfPages());
                ligne = 0;
            }
        }
        return lignes[ligne++];
    }

    @Override
    public void remove() {
    }
}
