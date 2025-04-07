import { ShopEntity } from "@/products/entities/shop-entity";

export const getShopLogoUrl = (shop: ShopEntity) => {
  switch (shop) {
    case ShopEntity.RTV_EURO_AGD:
      return "/shop/rtveuroagd_logo.webp";
    case ShopEntity.MORELE_NET:
      return "/shop/morele_logo.svg";
    case ShopEntity.MEDIA_EXPERT:
      return "/shop/mediaexpert_logo.svg";
    default:
      return "/no-image-placeholder.svg";
  }
};
