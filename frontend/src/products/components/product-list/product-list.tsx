"use client";
import { Button } from "@/core/components/ui/button";
import { H1 } from "@/core/components/ui/h1";
import { H2 } from "@/core/components/ui/h2";
import { SingleProduct, SortBar } from "@/products/components/index";
import { useFetchProductPage } from "@/products/hooks/client/use-fetch-product-page";
import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useQueryStates } from "nuqs";

const ProductList = () => {
  const [filters] = useQueryStates(productFiltersSearchParams);
  const [pagination, setPagination] = useQueryStates(
    productPaginationSearchParams,
  );

  const {
    data: productsList,
    isLoading,
    error,
    hasNextPage,
  } = useFetchProductPage(filters, pagination);

  if (isLoading) return <div className="text-primary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <>
      <div className="mb-1 flex justify-between">
        <H2>
          Produkty
        </H2>
        <SortBar />
      </div>
      <ul className="space-y-2">
        {productsList?.pages.map((page, pageIndex) => (
          <div key={pageIndex} className="space-y-1">
            {page.content.map((product) => (
              <li key={product.id}>
                <SingleProduct product={product} />
              </li>
            ))}
          </div>
        ))}
      </ul>
      <div className="mt-6 flex items-center justify-between">
        <Button
          className="bg-primary hover:bg-primary m-4 mt-0 px-4 py-2 text-white disabled:bg-gray-500 sm:m-0"
          onClick={() =>
            setPagination((prev) => ({
              ...prev,
              page: Math.max(prev.page - 1, 0),
            }))
          }
          disabled={pagination.page === 0}
        >
          <ChevronLeft />
        </Button>
        <span className="text-gray-700">Strona {pagination.page + 1}</span>
        <Button
          className="bg-primary hover:bg-hover m-4 mt-0 px-4 py-2 text-white disabled:bg-gray-500 sm:m-0"
          onClick={() =>
            setPagination((prev) => ({ ...prev, page: prev.page + 1 }))
          }
          disabled={!hasNextPage}
        >
          <ChevronRight />
        </Button>
      </div>
    </>
  );
};

export { ProductList };
