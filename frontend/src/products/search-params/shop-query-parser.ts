import {
  shopByDisplayNameMap,
  shopDisplayNameMap,
  ShopEntity,
} from "@/products/entities/shop-entity";
import { createParser } from "nuqs/server";

export const shopsQueryParser = createParser<ShopEntity>({
  parse(value) {
    return shopByDisplayNameMap[value] ?? null;
  },
  serialize(value) {
    return shopDisplayNameMap[value] ?? null;
  },
});
