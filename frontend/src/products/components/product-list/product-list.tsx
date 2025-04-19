"use client";

import { ProductPagination, SingleProduct } from "@/products/components";
import { useFetchProductPage } from "@/products/hooks/client/use-fetch-product-page";
import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";

export const ProductList = () => {
  const [productFilters] = useQueryStates(productFiltersSearchParams);
  const [productPagination] = useQueryStates(productPaginationSearchParams);

  const {
    data: productsPage,
    isLoading,
    error,
  } = useFetchProductPage(productFilters, {
    ...productPagination,
    page: Math.max(0, productPagination.page - 1),
  });

  if (isLoading) return <p className="text-primary pt-4">Ładowanie...</p>;
  if (error || !productsPage)
    return <div className="pt-4 text-red-600">Coś poszło nie tak!</div>;

  const hasNextPage =
    productsPage.page.number + 1 < productsPage.page.totalPages;

  return (
    <>
      <ul className="space-y-2">
        {productsPage.content.map((product) => (
          <li key={product.id}>
            <SingleProduct product={product} />
          </li>
        ))}
      </ul>

      <ProductPagination hasNextPage={hasNextPage} />
    </>
  );
};
