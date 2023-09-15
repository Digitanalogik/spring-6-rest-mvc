package fi.soininen.tatu.spring6restmvc.services;

import fi.soininen.tatu.spring6restmvc.model.BeerCSVRecord;

import java.io.File;
import java.util.List;

public interface BeerCsvService {
    List<BeerCSVRecord> convertCSV(File csvFile);
}
