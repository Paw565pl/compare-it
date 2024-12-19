package it.compare.backend.scraping.morele.dto;

import java.util.List;
import java.util.Optional;

public record MoreleProduct(
        String name,
        Prices prices,
        String productGroupName,
        Identifiers identifiers,
        Optional<OutletDetails> outletDetails,
        Optional<VoucherDetails> voucherDetails,
        List<Image> images,
        List<String> eanCodes) {
    public record Prices(Long mainPrice, Long promotionalPrice, Long voucherDiscountedPrice, Long unitPrice) {}

    public record Image(String url, String type) {}

    public record Identifiers(String productLinkName, String productGroupLinkName) {}

    public record VoucherDetails(String voucherName, String voucherCode, String beginTime, String endTime) {}

    public record OutletDetails(List<OutletCategory> outletCategories) {}

    public record OutletCategory(Long price) {}
}
