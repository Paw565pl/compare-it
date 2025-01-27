"use client";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { useFetchCategoriesList } from "@/products/hooks/client/use-fetch-categories-list";
import { parseAsFloat, parseAsString, useQueryStates } from "nuqs";

const CategoriesList = () => {
  const queryClient = getQueryClient();
  prefetchCategoriesList(queryClient);
  const { data: categoriesList, isLoading, error } = useFetchCategoriesList();

  const [, setFilters] = useQueryStates({
    category: parseAsString,
    minPrice: parseAsFloat,
    maxPrice: parseAsFloat,
    shop: parseAsString,
  });
  if (isLoading) return <div className="text-secondary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <HydrationBoundary dehydrate={queryClient}>
      <div className="flex flex-col items-start">
        <h2 className="mb-1 ml-4 text-2xl font-bold text-secondary sm:ml-0">
          Kategorie
        </h2>
        <ul className="mt-2 w-full bg-white">
          {categoriesList?.map((category, index) => (
            <li
              key={index}
              className="cursor-pointer transition-colors duration-200 hover:bg-hover hover:text-white"
            >
              <button
                onClick={() => setFilters({ category: category })}
                className="flex w-full justify-start px-4 py-2"
              >
                <div>{category}</div>
              </button>
            </li>
          ))}
        </ul>
      </div>
    </HydrationBoundary>
  );
};

export { CategoriesList };
