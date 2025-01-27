"use client";
import { useFetchProductPage } from "@/products/hooks/client/use-fetch-product-page";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useQueryStates } from "nuqs";
import { useEffect } from "react";
import { SingleProduct, SortBar } from "../index";

const ProductList = () => {
  const [filters, setFilters] = useQueryStates({
    category: "Karty graficzne",
    minPrice: 100,
    maxPrice: 15000,
    shop: "Morele.net,RTV Euro AGD",
  });
  const [pagination, setPagination] = useQueryStates({
    page: 1,
    size: 10,
    sort: "lowestCurrentPrice",
  });

  // to initialize default url params as we dont have the home page with proper buttons
  useEffect(() => {
    if (!filters.category)
      setFilters({
        category: "Karty graficzne",
        minPrice: 100,
        maxPrice: 15000,
        shop: "Morele.net,RTV Euro AGD",
      });

    if (!pagination.size)
      setPagination({ page: 0, size: 10, sort: "lowestCurrentPrice" });
  }, [filters, pagination, setFilters, setPagination]);

  const {
    data: productsList,
    isLoading,
    error,
    hasNextPage,
  } = useFetchProductPage(filters, pagination);

  if (isLoading) return <div className="text-secondary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <div>
      <div className="mb-1 flex justify-between">
        <h1 className="mb-2 ml-4 text-2xl font-bold text-secondary sm:ml-0">
          Produkty
        </h1>
        <SortBar />
      </div>
      <ul className="space-y-2">
        {productsList?.pages.map((page, pageIndex) => (
          <div key={pageIndex} className="space-y-1">
            {page.content.map((product) => (
              <li key={product.id}>
                <SingleProduct
                  category={product.category}
                  ean={product.ean}
                  id={product.id}
                  isAvailable={product.isAvailable}
                  lowestCurrentPrice={product.lowestCurrentPrice}
                  lowestPriceShop={product.lowestPriceShop}
                  mainImageUrl={product.mainImageUrl}
                  name={product.name}
                  offerCount={product.offerCount}
                />
              </li>
            ))}
          </div>
        ))}
      </ul>
      <div className="mt-6 flex items-center justify-between">
        <button
          className="m-4 mt-0 bg-secondary px-4 py-2 text-white hover:bg-secondary disabled:bg-gray-500 sm:m-0"
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
          className="m-4 mt-0 bg-secondary px-4 py-2 text-white hover:bg-hover disabled:bg-gray-500 sm:m-0"
          onClick={() =>
            setPagination((prev) => ({ ...prev, page: prev.page + 1 }))
          }
          disabled={!hasNextPage}
        >
          <ChevronRight />
        </button>
      </div>
    </div>
  );
};

export { ProductList };
