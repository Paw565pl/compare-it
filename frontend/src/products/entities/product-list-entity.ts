import { CategoryEntity } from "@/products/entities/category-entity";
import { CurrencyEntity } from "@/products/entities/currency-entity";
import { ShopEntity } from "@/products/entities/shop-entity";

export interface ProductListEntity {
  readonly id: string;
  readonly name: string;
  readonly ean: string;
  readonly category: CategoryEntity;
  readonly mainImageUrl: string | null;
  readonly lowestCurrentPrice: number | null;
  readonly lowestPriceCurrency: CurrencyEntity | null;
  readonly lowestPriceShop: ShopEntity | null;
  readonly availableOffersCount: number;
}
