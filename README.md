# jlox - Java Lox Language Interpreter

Interpreter writen in Java for Lox language

# Example

<pre>
<code>
<span style="color: #6A8759;">// Base class: Character</span>
<span style="color: #CC7832;">class</span> <span style="color: #A5C261;">Character</span> {
    <span style="color: #4EA1D3;">init</span>(name, health, attackPower) {
        <span style="color: #CC7832;">this</span>.name = name;
        <span style="color: #CC7832;">this</span>.health = health;
        <span style="color: #CC7832;">this</span>.attackPower = attackPower;
    }

    <span style="color: #4EA1D3;">attack</span>(target) {
        <span style="color: #6897BB;">print</span> <span style="color: #CC7832;">this</span>.name + <span style="color: #E6DB74;">" attacks "</span> + target.name + <span style="color: #E6DB74;">" for "</span> + <span style="color: #CC7832;">this</span>.attackPower + <span style="color: #E6DB74;">" damage!"</span>;
        target.<span style="color: #6897BB;">takeDamage</span>(<span style="color: #CC7832;">this</span>.attackPower);
    }

    <span style="color: #4EA1D3;">takeDamage</span>(amount) {
        <span style="color: #CC7832;">this</span>.health = <span style="color: #CC7832;">this</span>.health - amount;
        <span style="color: #CC7832;">if</span> (<span style="color: #CC7832;">this</span>.health < <span style="color: #9AC1F8;">0</span>) <span style="color: #CC7832;">this</span>.health = <span style="color: #9AC1F8;">0</span>;
        <span style="color: #6897BB;">print</span> <span style="color: #CC7832;">this</span>.name + <span style="color: #E6DB74;">" now has "</span> + <span style="color: #CC7832;">this</span>.health + <span style="color: #E6DB74;">" health."</span>;
    }

    <span style="color: #4EA1D3;">isAlive</span>() {
        <span style="color: #CC7832;">return</span> <span style="color: #CC7832;">this</span>.health > <span style="color: #9AC1F8;">0</span>;
    }

}

<span style="color: #6A8759;">// Subclass: Warrior</span>
<span style="color: #CC7832;">class</span> <span style="color: #A5C261;">Warrior</span> < <span style="color: #A5C261;">Character</span> {
    <span style="color: #4EA1D3;">init</span>(name, health, attackPower, armor) {
        <span style="color: #CC7832;">super</span>.<span style="color: #6897BB;">init</span>(name, health, attackPower);
        <span style="color: #CC7832;">this</span>.armor = armor;
    }

    <span style="color: #4EA1D3;">takeDamage</span>(amount) {
        <span style="color: #CC7832;">var</span> reducedDamage = amount - <span style="color: #CC7832;">this</span>.armor;
        <span style="color: #CC7832;">if</span> (reducedDamage < <span style="color: #9AC1F8;">0</span>) reducedDamage = <span style="color: #9AC1F8;">0</span>;
        <span style="color: #6897BB;">print</span> <span style="color: #CC7832;">this</span>.name + <span style="color: #E6DB74;">"'s armor reduces damage to "</span> + reducedDamage + <span style="color: #E6DB74;">"!"</span>;
        <span style="color: #CC7832;">super</span>.<span style="color: #6897BB;">takeDamage</span>(reducedDamage);
    }

}

<span style="color: #6A8759;">// Subclass: Mage</span>
<span style="color: #CC7832;">class</span> <span style="color: #A5C261;">Mage</span> < <span style="color: #A5C261;">Character</span> {
    <span style="color: #4EA1D3;">init</span>(name, health, attackPower, mana) {
        <span style="color: #CC7832;">super</span>.<span style="color: #6897BB;">init</span>(name, health, attackPower);
        <span style="color: #CC7832;">this</span>.mana = mana;
    }

    <span style="color: #4EA1D3;">castSpell</span>(target) {
        <span style="color: #CC7832;">if</span> (<span style="color: #CC7832;">this</span>.mana >= <span style="color: #9AC1F8;">10</span>) {
            <span style="color: #CC7832;">var</span> spellDamage = <span style="color: #CC7832;">this</span>.attackPower * <span style="color: #9AC1F8;">2</span>;
            <span style="color: #6897BB;">print</span> <span style="color: #CC7832;">this</span>.name + <span style="color: #E6DB74;">" casts a spell on "</span> + target.name + <span style="color: #E6DB74;">" for "</span> + spellDamage + <span style="color: #E6DB74;">" damage!"</span>;
            target.<span style="color: #6897BB;">takeDamage</span>(spellDamage);
            <span style="color: #CC7832;">this</span>.mana = <span style="color: #CC7832;">this</span>.mana - <span style="color: #9AC1F8;">10</span>;
            <span style="color: #6897BB;">print</span> <span style="color: #CC7832;">this</span>.name + <span style="color: #E6DB74;">" now has "</span> + <span style="color: #CC7832;">this</span>.mana + <span style="color: #E6DB74;">" mana."</span>;
        } <span style="color: #CC7832;">else</span> {
            <span style="color: #6897BB;">print</span> <span style="color: #CC7832;">this</span>.name + <span style="color: #E6DB74;">" doesn't have enough mana to cast a spell!"</span>;
        }
    }

}

// Create characters
<span style="color: #CC7832;">var</span> warrior = <span style="color: #A5C261;">Warrior</span>(<span style="color: #E6DB74;">"Thorin"</span>, <span style="color: #9AC1F8;">100</span>, <span style="color: #9AC1F8;">15</span>, <span style="color: #9AC1F8;">5</span>);
<span style="color: #CC7832;">var</span> mage = <span style="color: #A5C261;">Mage</span>(<span style="color: #E6DB74;">"Gandalf"</span>, <span style="color: #9AC1F8;">80</span>, <span style="color: #9AC1F8;">10</span>, <span style="color: #9AC1F8;">30</span>);
<span style="color: #CC7832;">var</span> enemy = <span style="color: #A5C261;">Character</span>(<span style="color: #E6DB74;">"Goblin"</span>, <span style="color: #9AC1F8;">50</span>, <span style="color: #9AC1F8;">8</span>);

// Start battle
<span style="color: #6897BB;">print</span> <span style="color: #E6DB74;">"Battle begins!"</span>;
<span style="color: #6897BB;">print</span> <span style="color: #E6DB74;">""</span>;

<span style="color: #CC7832;">while</span> (enemy.<span style="color: #6897BB;">isAlive</span>()) {
    warrior.<span style="color: #6897BB;">attack</span>(enemy);
    <span style="color: #6897BB;">print</span> <span style="color: #E6DB74;">""</span>;

    <span style="color: #CC7832;">if</span> (!enemy.<span style="color: #6897BB;">isAlive</span>())
        <span style="color: #CC7832;">break</span>;

    mage.<span style="color: #6897BB;">castSpell</span>(enemy);
    <span style="color: #6897BB;">print</span> <span style="color: #E6DB74;">""</span>;

    <span style="color: #CC7832;">if</span> (!enemy.<span style="color: #6897BB;">isAlive</span>())
        <span style="color: #CC7832;">break</span>;

    enemy.<span style="color: #6897BB;">attack</span>(warrior);
    <span style="color: #6897BB;">print</span> <span style="color: #E6DB74;">""</span>;

}

<span style="color: #6897BB;">print</span> enemy.name + <span style="color: #E6DB74;">" has been defeated!"</span>;
<span style="color: #6897BB;">print</span> <span style="color: #E6DB74;">"Battle ended!"</span>;

<span style="color: #6897BB;">print</span> <span style="color: #E6DB74;">""</span>;
<span style="color: #6897BB;">print</span> <span style="color: #E6DB74;">"Program executed in "</span> + (clock() / <span style="color: #9AC1F8;">1000</span>) + <span style="color: #E6DB74;">"s"</span>;
</code>
</pre>