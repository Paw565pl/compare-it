package it.compare.backend.scraping.rtveuroagd.dto;

import java.util.List;
import java.util.Optional;

public record RtvEuroAgdProduct(
        String name,
        Prices prices,
        String productGroupName,
        Identifiers identifiers,
        Optional<OutletDetails> outletDetails,
        Optional<VoucherDetails> voucherDetails,
        List<Image> images,
        List<String> eanCodes) {

    public record Prices(
            Long mainPrice, PromotionalPrice promotionalPrice, Long voucherDiscountedPrice, Long unitPrice) {}

    public record PromotionalPrice(Long price) {}

    public record Image(String url, String type) {}

    public record Identifiers(String productLinkName, String productGroupLinkName) {}

    public record VoucherDetails(String voucherName, String voucherCode, String beginTime, String endTime) {}

    public record OutletDetails(List<OutletCategory> outletCategories) {}

    public record OutletCategory(Long price) {}
}
