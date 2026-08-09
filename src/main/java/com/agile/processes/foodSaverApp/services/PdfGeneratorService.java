package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.NGOAnalyticsDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantAnalyticsDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfGeneratorService {

    public byte[] generateRestaurantAnalyticsPdf(RestaurantAnalyticsDTO analytics) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Monthly Analytics Report: " + analytics.getRestaurantName(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            addTableRow(table, "Total Donations Count", String.valueOf(analytics.getTotalDonationsCount()));
            addTableRow(table, "Active Donations Count", String.valueOf(analytics.getActiveDonationsCount()));
            addTableRow(table, "Claimed Donations Count", String.valueOf(analytics.getClaimedDonationsCount()));
            addTableRow(table, "Expired Donations Count", String.valueOf(analytics.getExpiredDonationsCount()));
            addTableRow(table, "Total Pickups Received", String.valueOf(analytics.getTotalPickupsReceived()));
            addTableRow(table, "Pending Pickups", String.valueOf(analytics.getPendingPickupsCount()));
            addTableRow(table, "Completed Pickups", String.valueOf(analytics.getCompletedPickupsCount()));
            addTableRow(table, "Cancelled Pickups", String.valueOf(analytics.getCancelledPickupsCount()));

            document.add(table);

            document.add(new Paragraph("\nTotal Quantity Posted by Unit:"));
            document.add(createMapTable(analytics.getTotalQuantityPostedByUnit()));

            document.add(new Paragraph("\nTotal Quantity Completed by Unit:"));
            document.add(createMapTable(analytics.getTotalQuantityCompletedByUnit()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    public byte[] generateNGOAnalyticsPdf(NGOAnalyticsDTO analytics) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Monthly Analytics Report: " + analytics.getNgoName(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            addTableRow(table, "Total Pickups Requested", String.valueOf(analytics.getTotalPickupsRequested()));
            addTableRow(table, "Pending Pickups", String.valueOf(analytics.getPendingPickupsCount()));
            addTableRow(table, "Completed Pickups", String.valueOf(analytics.getCompletedPickupsCount()));
            addTableRow(table, "Cancelled Pickups", String.valueOf(analytics.getCancelledPickupsCount()));

            document.add(table);

            document.add(new Paragraph("\nTotal Quantity Claimed by Unit:"));
            document.add(createMapTable(analytics.getTotalQuantityClaimedByUnit()));

            document.add(new Paragraph("\nTotal Quantity Completed by Unit:"));
            document.add(createMapTable(analytics.getTotalQuantityCompletedByUnit()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String header, String value) {
        PdfPCell headerCell = new PdfPCell(new Phrase(header));
        PdfPCell valueCell = new PdfPCell(new Phrase(value));
        table.addCell(headerCell);
        table.addCell(valueCell);
    }

    private PdfPTable createMapTable(Map<String, Integer> mapData) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        if (mapData == null || mapData.isEmpty()) {
            addTableRow(table, "No data", "-");
            return table;
        }

        addTableRow(table, "Unit", "Quantity");
        for (Map.Entry<String, Integer> entry : mapData.entrySet()) {
            addTableRow(table, entry.getKey(), String.valueOf(entry.getValue()));
        }
        return table;
    }
}
