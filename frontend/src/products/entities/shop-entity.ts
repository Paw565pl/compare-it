export enum ShopEntity {
  RTV_EURO_AGD = "RTV_EURO_AGD",
  MEDIA_EXPERT = "MEDIA_EXPERT",
  MORELE_NET = "MORELE_NET",
}

export const shopsHumanReadableNames: Record<ShopEntity, string> = {
  [ShopEntity.RTV_EURO_AGD]: "RTV Euro AGD",
  [ShopEntity.MEDIA_EXPERT]: "Media Expert",
  [ShopEntity.MORELE_NET]: "Morele.net",
} as const;
