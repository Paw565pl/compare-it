"use client";

import { ProductPagination, SingleProduct } from "@/products/components";
import { useFetchProductPage } from "@/products/hooks/client/use-fetch-product-page";
import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";

export const ProductList = () => {
  const [filters] = useQueryStates(productFiltersSearchParams);
  const [pagination] = useQueryStates(productPaginationSearchParams, {
    scroll: true,
  });

  const {
    data: productsPage,
    isLoading,
    error,
  } = useFetchProductPage(filters, {
    ...pagination,
    page: Math.max(0, pagination.page - 1),
  });

  if (isLoading) return <p className="text-primary pt-4">Ładowanie...</p>;
  if (error || !productsPage)
    return <div className="text-red-600">Coś poszło nie tak!</div>;

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
