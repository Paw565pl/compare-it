"use client";
import { useFetchCategoriesList } from "@/products/hooks/client/use-fetch-categories-list";

const CategoriesList = () => {
  const { data: categoriesList, isLoading, error } = useFetchCategoriesList();

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Something went wrong!</div>;

  return (
    <div className="flex flex-col items-start rounded-lg bg-white p-2 text-blue-700">
      <h2 className="text-2xl font-bold">Categories</h2>
      <ul>
        {categoriesList?.map((category, index) => (
          <li key={index}>{category}</li>
        ))}
      </ul>
    </div>
  );
};

export { CategoriesList };
