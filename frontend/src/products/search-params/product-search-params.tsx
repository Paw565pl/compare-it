import {
  createLoader,
  parseAsBoolean,
  parseAsFloat,
  parseAsInteger,
  parseAsString,
} from "nuqs/server";

export const productFiltersSearchParams = {
  name: parseAsString,
  category: parseAsString,
  minPrice: parseAsFloat,
  maxPrice: parseAsFloat,
  isAvailable: parseAsBoolean,
  shop: parseAsString,
} as const;

export const loadProductFiltersSearchParams = createLoader(
  productFiltersSearchParams,
);

export const productPaginationSearchParams = {
  page: parseAsInteger.withDefault(1),
  size: parseAsInteger.withDefault(20),
  sort: parseAsString,
} as const;

export const loadProductPaginationSearchParams = createLoader(
  productPaginationSearchParams,
);
