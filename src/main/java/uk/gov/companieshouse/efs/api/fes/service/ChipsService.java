package uk.gov.companieshouse.efs.api.fes.service;

import java.util.List;

public interface ChipsService {

    List<String> getRejectReasons(String barcode);

}
