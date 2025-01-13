"use client";
import { useFetchCategoriesList } from "@/products/hooks/client/use-fetch-categories-list";

const CategoriesList = () => {
  const { data: categoriesList, isLoading, error } = useFetchCategoriesList();

  if (isLoading) return <div className="text-blue-700">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <div className="flex flex-col items-start rounded-lg border border-gray-200 bg-white p-4 shadow-md">
      <h2 className="mb-4 text-2xl font-bold text-blue-700">Kategorie</h2>
      <ul className="space-y-3">
        {categoriesList?.map((category, index) => (
          <li
            key={index}
            className="cursor-pointer rounded-lg px-4 py-2 transition-colors duration-200 hover:bg-blue-700 hover:text-white"
          >
            {category}
          </li>
        ))}
      </ul>
    </div>
  );
};

export { CategoriesList };
