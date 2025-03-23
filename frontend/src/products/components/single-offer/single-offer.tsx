import { Button } from "@/core/components/ui/button";
import { ImageWithFallback } from "@/core/components/ui/image-with-fallback";
import { formatCurrency } from "@/core/utils/format-currency";
import { OfferEntity } from "@/products/entities/product-detail-entity";
import Link from "next/link";

interface SingleOfferProps {
  offer: OfferEntity;
}

const SingleOffer = ({ offer }: SingleOfferProps) => {
  const formattedPrice = formatCurrency(
    offer.priceHistory[offer.priceHistory.length - 1].price,
    offer.priceHistory[offer.priceHistory.length - 1].currency,
  );

  return (
    <div className="flex flex-col items-center justify-between bg-white p-4 sm:flex-row">
      <Link href={offer.url} target="_blank" rel="nofollow noopener">
        <ImageWithFallback
          name={offer.shop}
          imageUrl={offer.shopLogoUrl}
          width={200}
          height={200}
        />
      </Link>
      <div className="flex items-center">
        <div className="mr-4 flex flex-col items-center">
          <Link
            href={offer.url}
            target="_blank"
            rel="nofollow noopener"
            className="text-primary justify-center text-lg font-semibold"
          >
            {formattedPrice}
          </Link>
          <div className="text-sm">
            {offer.priceHistory[offer.priceHistory.length - 1].isAvailable ? (
              <span className="font-semibold text-green-600">
                Produkt dostępny
              </span>
            ) : (
              <span className="font-semibold text-red-600">
                Produkt niedostępny
              </span>
            )}
          </div>
        </div>
        <Button
          asChild
          className="bg-primary hover:bg-hover font-semibold shadow-none"
        >
          <Link href={offer.url} target="_blank" rel="nofollow noopener">
            PRZEJDŹ DO OFERTY
          </Link>
        </Button>
      </div>
    </div>
  );
};

export { SingleOffer };
