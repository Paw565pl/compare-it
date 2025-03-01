"use client";
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

  if (isLoading) return <div className="text-secondary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <>
      <div className="mb-1 flex justify-between">
        <h1 className="text-secondary mb-2 ml-4 text-2xl font-bold sm:ml-0">
          Produkty
        </h1>
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
        <button
          className="bg-secondary hover:bg-secondary m-4 mt-0 px-4 py-2 text-white disabled:bg-gray-500 sm:m-0"
          onClick={() =>
            setPagination((prev) => ({
              ...prev,
              page: Math.max(prev.page - 1, 0),
            }))
          }
          disabled={pagination.page === 0}
        >
          <ChevronLeft />
        </button>
        <span className="text-gray-700">Strona {pagination.page + 1}</span>
        <button
          className="bg-secondary hover:bg-hover m-4 mt-0 px-4 py-2 text-white disabled:bg-gray-500 sm:m-0"
          onClick={() =>
            setPagination((prev) => ({ ...prev, page: prev.page + 1 }))
          }
          disabled={!hasNextPage}
        >
          <ChevronRight />
        </button>
      </div>
    </>
  );
};

export { ProductList };
