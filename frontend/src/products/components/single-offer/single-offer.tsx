import { Button } from "@/core/components/ui/button";
import { ImageWithFallback } from "@/core/components/ui/image-with-fallback";
import { formatCurrency } from "@/core/utils/format-currency";
import { OfferEntity } from "@/products/entities/offer-entity";
import { getShopLogoUrl } from "@/products/utils/get-shop-logo-url";
import Link from "next/link";

interface SingleOfferProps {
  readonly offer: OfferEntity;
}

export const SingleOffer = ({ offer }: SingleOfferProps) => {
  const lastPrice = offer.priceHistory.at(-1)?.price;
  const lastCurrency = offer.priceHistory.at(-1)?.currency;

  const formattedPrice =
    lastPrice && lastCurrency ? formatCurrency(lastPrice, lastCurrency) : "-";

  return (
    <div className="flex flex-col items-center justify-between bg-white p-4 sm:flex-row">
      <Link
        href={offer.url}
        target="_blank"
        rel="nofollow noopener"
        className="bg-background mb-3 px-2 py-4 sm:mb-0"
      >
        <ImageWithFallback
          src={getShopLogoUrl(offer.shop)}
          alt={offer.shop}
          fill
          sizes="(max-width: 359px) 100vw, 320px"
          containerClassName="w-40 h-6.25"
        />
      </Link>

      <div className="flex items-center">
        <div className="mr-4 flex flex-col items-center">
          <span className="text-primary justify-center text-lg font-semibold">
            {formattedPrice}
          </span>
          <div className="text-sm">
            {offer.isAvailable ? (
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
