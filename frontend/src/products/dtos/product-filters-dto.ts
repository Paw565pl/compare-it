import { CategoryEntity } from "@/products/entities/category-entity";
import { ShopEntity } from "@/products/entities/shop-entity";

export interface ProductFiltersDto {
  name: string | null;
  category: CategoryEntity | null;
  shops: ShopEntity[] | null;
  minPrice: number | null;
  maxPrice: number | null;
  isAvailable: boolean | null;
}
