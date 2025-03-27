import { ShopEntity } from "@/products/entities/shop-entity";

export const getShopLogoUrl = (shop: ShopEntity) => {
  switch (shop) {
    case ShopEntity.RTV_EURO_AGD:
      return "/shop/rtveuroagd_logo.webp";
    case ShopEntity.MORELE_NET:
      return "/shop/morele_logo.webp";
    default:
      return "/no-image-placeholder.svg";
  }
};
