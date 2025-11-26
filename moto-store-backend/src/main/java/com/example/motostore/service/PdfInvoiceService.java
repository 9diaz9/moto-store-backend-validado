package com.example.motostore.service;

import com.example.motostore.model.Sale;
import com.example.motostore.model.SaleItem;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class PdfInvoiceService {

    public String generateInvoice(Sale sale) {
        try {
            // Carpeta destino
            String dir = "invoices";
            Files.createDirectories(Paths.get(dir));
            String filePath = dir + "/invoice_" + sale.getId() + ".pdf";

            // Formato de moneda (Colombia)
            NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

            Document document = new Document(PageSize.A4, 36, 36, 36, 36); // márgenes
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // =========================
            //  FUENTES
            // =========================
            Font titleFont      = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font subTitleFont   = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont     = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font smallGrayFont  = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(120, 120, 120));
            Font tableHeaderFont= new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font tableBodyFont  = new Font(Font.HELVETICA, 10, Font.NORMAL);

            // =========================
            //  ENCABEZADO
            // =========================
            Paragraph title = new Paragraph("MOTOSTORE SUZUKI", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4f);
            document.add(title);

            Paragraph subTitle = new Paragraph("Factura de venta de motos", subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(12f);
            document.add(subTitle);

            Paragraph nit = new Paragraph("NIT: 900.000.000-1  ·  Bogotá D.C.", smallGrayFont);
            nit.setAlignment(Element.ALIGN_CENTER);
            nit.setSpacingAfter(18f);
            document.add(nit);

            // =========================
            //  DATOS GENERALES VENTA
            // =========================
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100f);
            infoTable.setSpacingAfter(15f);
            infoTable.setWidths(new float[]{1.2f, 2.8f});

            infoTable.addCell(buildInfoCell("ID Venta:", true, normalFont));
            infoTable.addCell(buildInfoCell(String.valueOf(sale.getId()), false, normalFont));

            infoTable.addCell(buildInfoCell("Cliente:", true, normalFont));
            infoTable.addCell(buildInfoCell(
                    sale.getCustomer() != null ? sale.getCustomer().getFullName() : "N/A",
                    false, normalFont
            ));

            infoTable.addCell(buildInfoCell("Fecha:", true, normalFont));
            infoTable.addCell(buildInfoCell(
                    sale.getSaleDate() != null ? sale.getSaleDate().toString() : "N/A",
                    false, normalFont
            ));

            document.add(infoTable);

            // =========================
            //  TABLA DE ÍTEMS
            // =========================
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100f);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(15f);
            table.setWidths(new float[]{3.5f, 1.0f, 1.6f, 1.6f});

            // Encabezados
            table.addCell(buildHeaderCell("Moto", tableHeaderFont));
            table.addCell(buildHeaderCell("Cantidad", tableHeaderFont));
            table.addCell(buildHeaderCell("Precio unitario", tableHeaderFont));
            table.addCell(buildHeaderCell("Subtotal", tableHeaderFont));

            // Filas
            if (sale.getItems() != null) {
                for (SaleItem item : sale.getItems()) {
                    String motoName = item.getMoto().getBrand() + " " + item.getMoto().getModel();

                    // Moto
                    PdfPCell motoCell = new PdfPCell(new Phrase(motoName, tableBodyFont));
                    motoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    motoCell.setPadding(6f);
                    table.addCell(motoCell);

                    // Cantidad
                    PdfPCell qtyCell = new PdfPCell(
                            new Phrase(String.valueOf(item.getQuantity()), tableBodyFont));
                    qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    qtyCell.setPadding(6f);
                    table.addCell(qtyCell);

                    // Precio unitario
                    PdfPCell unitPriceCell = new PdfPCell(
                            new Phrase(moneyFormat.format(item.getUnitPrice()), tableBodyFont));
                    unitPriceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    unitPriceCell.setPadding(6f);
                    table.addCell(unitPriceCell);

                    // Subtotal
                    PdfPCell subtotalCell = new PdfPCell(
                            new Phrase(moneyFormat.format(item.getSubtotal()), tableBodyFont));
                    subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    subtotalCell.setPadding(6f);
                    table.addCell(subtotalCell);
                }
            }

            document.add(table);

            // =========================
            //  TOTAL
            // =========================
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40f);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setWidths(new float[]{1.4f, 1.6f});

            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL A PAGAR:", subTitleFont));
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabel.setPadding(6f);
            totalLabel.setBorder(Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM);

            PdfPCell totalValue = new PdfPCell(
                    new Phrase(moneyFormat.format(sale.getTotal()), subTitleFont));
            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValue.setPadding(6f);
            totalValue.setBorder(Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM);
            totalValue.setBackgroundColor(new Color(230, 244, 250));

            totalTable.addCell(totalLabel);
            totalTable.addCell(totalValue);

            document.add(totalTable);

            // =========================
            //  MENSAJE FINAL / PIE
            // =========================
            Paragraph thanks = new Paragraph(
                    "\nGracias por su compra.\n" +
                    "Por favor conserve esta factura como soporte de su operación.",
                    normalFont
            );
            thanks.setSpacingBefore(18f);
            thanks.setAlignment(Element.ALIGN_LEFT);
            document.add(thanks);

            Paragraph footer = new Paragraph(
                    "MotoStore Suzuki · Tel: (601) 000 00 00 · www.motostore-suzuki.com",
                    smallGrayFont
            );
            footer.setSpacingBefore(20f);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return filePath;

        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    // =========================
    //  HELPERS DE CÉLULAS
    // =========================

    private PdfPCell buildInfoCell(String text, boolean bold, Font baseFont) {
        Font f = bold
                ? new Font(baseFont.getFamily(), baseFont.getSize(), Font.BOLD)
                : baseFont;

        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3f);
        return cell;
    }

    private PdfPCell buildHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6f);
        cell.setBackgroundColor(new Color(15, 23, 42)); // azul oscuro tipo Suzuki
        cell.setBorderColor(new Color(148, 163, 184));
        return cell;
    }
}
