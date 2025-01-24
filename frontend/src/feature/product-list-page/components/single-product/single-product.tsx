import { SingleProduct as SingleProductProps } from "@/feature/product-list-page/types/single-product";
import Image from "next/image";
import Link from "next/link";

const SingleProduct = ({
  category,
  ean,
  id,
  isAvailable,
  lowestCurrentPrice,
  lowestPriceShop,
  mainImageUrl,
  name,
  offerCount,
}: SingleProductProps) => {
  return (
    <div className="border-grey-100 flex flex-col rounded-lg border-2 bg-white p-6 text-blue-700 md:flex-row">
      {/* Produkt: Obrazek */}
      <div className="mb-4 flex-shrink-0 md:mb-0 md:mr-6">
        <Image
          src={mainImageUrl}
          width={200}
          height={200}
          alt={name}
          className="rounded-md"
        />
      </div>

      {/* Produkt: Szczegóły */}
      <div className="flex flex-grow flex-col justify-between">
        {/* Górna sekcja */}
        <div>
          <h1 className="mb-2 text-xl font-bold">{name}</h1>
          <p className="mb-4 text-sm text-gray-500">Kategoria: {category}</p>

          {/* Dostępność */}
          <div className="mb-4 text-sm">
            {isAvailable ? (
              <span className="font-semibold text-green-600">
                Produkt dostępny
              </span>
            ) : (
              <span className="font-semibold text-red-600">
                Produkt niedostępny
              </span>
            )}
          </div>

          {/* Liczba ofert */}
          <p className="text-sm text-gray-600">Liczba ofert: {offerCount}</p>
        </div>

        {/* Dolna sekcja */}
        <div className="mt-4">
          <p className="mb-2 text-lg font-semibold text-blue-700">
            Najniższa cena: {lowestCurrentPrice} zł
          </p>
          <p className="mb-4 text-sm text-gray-600">Sklep: {lowestPriceShop}</p>
          <Link
            href={`/product/${id}`}
            className="block rounded-lg bg-blue-700 px-4 py-2 text-center font-semibold text-white transition hover:bg-blue-600"
          >
            Porównaj oferty
          </Link>
        </div>
      </div>
    </div>
  );
};

export { SingleProduct };
