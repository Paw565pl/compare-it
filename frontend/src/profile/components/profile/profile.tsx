"use client";

import { Button } from "@/core/components/ui/button";
import { FavoriteProductsGrid } from "@/favorite-products/components/favorite-products-grid/favorite-products-grid";
import {
  PriceAlertsGrid,
  PriceAlertsNotificationsGrid,
} from "@/price-alerts/components";
import { UserDetailsCard } from "@/profile/components";
import { AlarmClock, Heart, NotebookPen, User } from "lucide-react";
import { ReactNode, useState } from "react";

interface AsideButton {
  readonly title: string;
  readonly component: ReactNode;
  readonly icon: ReactNode;
}

const asideButtonClassName = "w-7! h-7! !sm:w-10 !sm:h-10" as const;

export const Profile = () => {
  const [activeComponent, setActiveComponent] = useState<ReactNode>(
    <UserDetailsCard />,
  );
  const [activeButtonIndex, setActiveButtonIndex] = useState(0);

  const asideButtons: AsideButton[] = [
    {
      title: "Twój profil",
      component: <UserDetailsCard />,
      icon: <User className={asideButtonClassName} />,
    },
    {
      title: "Ulubione produkty",
      component: <FavoriteProductsGrid />,
      icon: <Heart className={asideButtonClassName} />,
    },
    {
      title: "Zarządzanie alertami",
      component: <PriceAlertsGrid />,
      icon: <NotebookPen className={asideButtonClassName} />,
    },
    {
      title: "Historia alertów",
      component: <PriceAlertsNotificationsGrid />,
      icon: <AlarmClock className={asideButtonClassName} />,
    },
  ] as const;

  return (
    <div className="flex flex-col items-center sm:flex-row sm:items-start sm:gap-6">
      <aside className="xs:flex-flex-nowrap mb-6 flex w-full flex-wrap gap-2 sm:mb-0 sm:w-1/12 sm:flex-col sm:gap-4">
        {asideButtons.map(({ title, component, icon }, index) => (
          <Button
            key={index}
            onClick={() => {
              setActiveComponent(component);
              setActiveButtonIndex(index);
            }}
            variant={activeButtonIndex === index ? "primary" : "outline"}
            size="icon"
            title={title}
            className="w-full p-6 sm:p-8"
          >
            {icon}
          </Button>
        ))}
      </aside>

      <section className="w-full">{activeComponent}</section>
    </div>
  );
};
