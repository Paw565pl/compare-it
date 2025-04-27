import { ShopEntity } from "@/products/entities/shop-entity";
import noImagePlaceholder from "@public/no-image-placeholder.svg";
import mediaExpertLogo from "@public/shop/mediaexpert_logo.svg";
import moreleLogo from "@public/shop/morele_logo.svg";
import rtvEuroAgdLogo from "@public/shop/rtveuroagd_logo.webp";

export const getShopLogoUrl = (shop: ShopEntity) => {
  switch (shop) {
    case ShopEntity.RTV_EURO_AGD:
      return rtvEuroAgdLogo;
    case ShopEntity.MORELE_NET:
      return moreleLogo;
    case ShopEntity.MEDIA_EXPERT:
      return mediaExpertLogo;
    default:
      return noImagePlaceholder;
  }
};
