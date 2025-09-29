import {
  CategoryEntity,
  categoryByDisplayNameMap,
  categoryDisplayNameMap,
} from "@/products/entities/category-entity";
import { createParser } from "nuqs/server";

export const categoryQueryParser = createParser<CategoryEntity>({
  parse(value) {
    return categoryByDisplayNameMap[value] ?? null;
  },
  serialize(value) {
    return categoryDisplayNameMap[value].toLowerCase() ?? null;
  },
});
