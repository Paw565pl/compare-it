import { ProductImage } from "@/products/components";
import { ProductListEntity } from "@/products/entities/product-list-entity";
import { formatCurrency } from "@/products/utils/format-currency";
import Link from "next/link";

interface SingleProductProps {
  readonly product: ProductListEntity;
}

const SingleProduct = ({
  product: {
    category,
    id,
    isAvailable,
    lowestCurrentPrice,
    lowestPriceCurrency,
    lowestPriceShop,
    mainImageUrl,
    name,
    offerCount,
  },
}: SingleProductProps) => {
  const formattedPrice = formatCurrency(
    lowestCurrentPrice,
    lowestPriceCurrency,
  );

  return (
    <div className="border-grey-100 flex flex-col bg-white p-6 text-secondary md:flex-row">
      <div className="mb-4 flex-shrink-0 self-center md:mb-0 md:mr-6">
        <Link href={`/produkty/${id}`}>
          <ProductImage name={name} imageUrl={mainImageUrl} />
        </Link>
      </div>

      <div className="flex flex-grow flex-col justify-between">
        <div>
          <Link href={`/produkty/${id}`}>
            <h1 className="mb-2 text-xl font-bold">{name}</h1>
          </Link>
          <p className="mb-4 text-sm text-gray-500">Kategoria: {category}</p>

          <p className="text-sm text-gray-600">Liczba ofert: {offerCount}</p>
        </div>

        <div className="mt-1">
          <p className="mb-1 text-sm text-gray-600">Sklep: {lowestPriceShop}</p>
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
          <p className="mb-2 text-lg font-semibold text-secondary">
            Najniższa cena: {formattedPrice}
          </p>
          <Link
            href={`/produkty/${id}`}
            className="block bg-secondary px-4 py-2 text-center font-semibold text-white transition hover:bg-hover"
          >
            Porównaj oferty
          </Link>
        </div>
      </div>
    </div>
  );
};

export { SingleProduct };
