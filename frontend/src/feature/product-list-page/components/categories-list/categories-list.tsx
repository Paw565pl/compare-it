"use client";
import { useFetchCategoriesList } from "@/products/hooks/client/use-fetch-categories-list";
import { useQueryStates } from "nuqs";

const CategoriesList = () => {
  const { data: categoriesList, isLoading, error } = useFetchCategoriesList();

  const [_, setFilters] = useQueryStates({ category: "Karty graficzne" });

  if (isLoading) return <div className="text-blue-700">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <div className="flex flex-col items-start">
      <h2 className="text-2xl font-bold text-blue-700">Kategorie</h2>
      <ul className="mt-2 w-full space-y-3 bg-white">
        {categoriesList?.map((category, index) => (
          <li
            key={index}
            className="cursor-pointer rounded-lg px-4 py-2 transition-colors duration-200 hover:bg-blue-700 hover:text-white"
          >
            <button onClick={() => setFilters({ category: category })}>
              {category}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export { CategoriesList };
