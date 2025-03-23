import { H1 } from "@/core/components/ui/h1";
import { ImageWithFallback } from "@/core/components/ui/image-with-fallback";
import { formatCurrency } from "@/core/utils/format-currency";
import { ProductListEntity } from "@/products/entities/product-list-entity";
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
    <div className="border-grey-100 text-primary mt-1 flex flex-col bg-white p-6 md:flex-row">
      <div className="mb-4 shrink-0 self-center md:mr-6 md:mb-0">
        <Link href={`/produkty/${id}`}>
          <ImageWithFallback
            name={name}
            imageUrl={mainImageUrl}
            width={200}
            height={200}
          />
        </Link>
      </div>

      <div className="flex grow flex-col justify-between">
        <div>
          <Link href={`/produkty/${id}`}>
            <H1 className="mb-2 text-xl font-bold">{name}</H1>
          </Link>
          <p className="text-secondary mb-4 text-sm">Kategoria: {category}</p>

          <p className="text-secondary text-sm">Liczba ofert: {offerCount}</p>
        </div>

        <div className="mt-1">
          <p className="text-secondary mb-1 text-sm">
            Sklep: {lowestPriceShop}
          </p>
          <div className="mb-4 text-sm">
            {isAvailable ? (
              <span className="font-semibold text-green-500">
                Produkt dostępny
              </span>
            ) : (
              <span className="font-semibold text-red-500">
                Produkt niedostępny
              </span>
            )}
          </div>
          <p className="text-primary mb-2 text-lg font-semibold">
            Najniższa cena: {formattedPrice}
          </p>
          <Link
            href={`/produkty/${id}`}
            className="bg-primary hover:bg-hover block px-4 py-2 text-center font-semibold text-white transition"
          >
            Porównaj oferty
          </Link>
        </div>
      </div>
    </div>
  );
};

export { SingleProduct };
