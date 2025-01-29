"use client";
import { useFetchCategoriesList } from "@/products/hooks/client/use-fetch-categories-list";
import { productFiltersSearchParams } from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";

const CategoriesList = () => {
  const { data: categoriesList } = useFetchCategoriesList();
  const [filters, setFilters] = useQueryStates(productFiltersSearchParams);

  return (
    <div className="flex flex-col items-start">
      <h2 className="mb-1 ml-4 text-2xl font-bold text-secondary sm:ml-0">
        Kategorie
      </h2>
      <ul className="mt-2 w-full bg-white">
        <li
          className={`cursor-pointer transition-colors duration-200 ${
            filters.category === null
              ? "bg-hover text-white"
              : "hover:bg-hover hover:text-white"
          }`}
        >
          <button
            onClick={() => setFilters({ category: null })}
            className="flex w-full justify-start px-4 py-2"
          >
            <span>Wszystkie</span>
          </button>
        </li>

        {categoriesList?.map((category, index) => {
          const isActive = filters.category === category;
          return (
            <li
              key={index}
              className={`cursor-pointer transition-colors duration-200 ${
                isActive
                  ? "bg-hover text-white"
                  : "hover:bg-hover hover:text-white"
              }`}
            >
              <button
                onClick={() => setFilters({ category })}
                className="flex w-full justify-start px-4 py-2"
              >
                <span>{category}</span>
              </button>
            </li>
          );
        })}
      </ul>
    </div>
  );
};

export { CategoriesList };
