"use client";
import { useFetchProductPage } from "@/products/hooks/client/use-fetch-product-page";
import { useQueryStates } from "nuqs";
import { useEffect } from "react";
import { SingleProduct } from "../index";

const ProductList = () => {
  const [filters, setFilters] = useQueryStates({
    category: "Karty graficzne",
    minPrice: 1900,
    maxPrice: 2000,
    shop: "Morele.net",
  });
  const [pagination, setPagination] = useQueryStates({ size: 20 });
  console.log("filters: ", filters);

  // to initialize default url params as we dont have the home page with proper buttons
  useEffect(() => {
    if (!filters.category)
      setFilters({
        category: "Karty graficzne",
        minPrice: 1900,
        maxPrice: 2000,
      });

    if (!pagination.size) setPagination({ size: 5 });
  }, [filters, pagination, setFilters, setPagination]);

  const { data: productsList } = useFetchProductPage(filters, pagination);

  return (
    <div className="w-full rounded-lg bg-white p-6 shadow-lg">
      <h1 className="mb-4 text-2xl font-bold text-blue-700">Lista produktów</h1>
      <ul className="space-y-4">
        {productsList?.pages.map((page, pageIndex) => (
          <div key={pageIndex} className="space-y-4">
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
          className="rounded-lg bg-blue-700 px-4 py-2 text-white hover:bg-blue-600"
          onClick={() =>
            setPagination((prev) => ({
              ...prev,
              page: Math.max(prev.page - 1, 1),
            }))
          }
          disabled={pagination.page === 1}
        >
          Poprzednia
        </button>
        <span className="text-gray-700">Strona {pagination.page}</span>
        <button
          className="rounded-lg bg-blue-700 px-4 py-2 text-white hover:bg-blue-600"
          onClick={() =>
            setPagination((prev) => ({ ...prev, page: prev.page + 1 }))
          }
          disabled={
            !productsList || productsList.pages.length < pagination.limit
          }
        >
          Następna
        </button>
      </div>
    </div>
  );
};

export { ProductList };
