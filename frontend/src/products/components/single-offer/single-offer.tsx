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
    <div className="flex flex-col items-center justify-between gap-y-3 bg-white p-4 sm:flex-row">
      <Link
        href={offer.url}
        target="_blank"
        rel="nofollow noopener"
        className="bg-background px-2 py-4"
      >
        <ImageWithFallback
          src={getShopLogoUrl(offer.shop)}
          alt={offer.shop}
          fill
          sizes="(max-width: 359px) 100vw, 320px"
          containerClassName="w-40 h-6.25"
          isLoadingStateEnabled={false}
        />
      </Link>

      <div className="flex flex-col items-center gap-x-4 gap-y-3 sm:flex-row">
        <div className="flex flex-row items-center gap-x-3 sm:flex-col">
          <span className="text-primary justify-center text-lg font-semibold">
            {formattedPrice}
          </span>

          {offer.isAvailable ? (
            <span className="text-sm font-semibold text-green-600">
              Produkt dostępny
            </span>
          ) : (
            <span className="text-sm font-semibold text-red-600">
              Produkt niedostępny
            </span>
          )}
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
